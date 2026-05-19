package noise

import (
	"BeeIOT/internal/domain/interfaces"
	"BeeIOT/internal/domain/models/dbTypes"
	"BeeIOT/internal/domain/notification"
	"context"
	"errors"
	"fmt"
	"math"
	"time"

	"github.com/rs/zerolog"
)

type Analyzer struct {
	period       time.Duration
	db           interfaces.DB
	ctx          context.Context
	notification *notification.Notification
	logger       zerolog.Logger
}

func NewAnalyzer(ctx context.Context, period time.Duration, db interfaces.DB, notification *notification.Notification) *Analyzer {
	logger := ctx.Value("logger").(zerolog.Logger)
	return &Analyzer{period: period, db: db, ctx: ctx, notification: notification, logger: logger}
}

func (a *Analyzer) Start() {
	go func() {
		a.analyzeNoise()
		ticker := time.NewTicker(a.period)
		defer ticker.Stop()
		for {
			select {
			case <-ticker.C:
				a.analyzeNoise()
			case <-a.ctx.Done():
				return
			}
		}
	}()
}

func (a *Analyzer) analyzeNoise() {
	a.logger.Info().Msg("noise analyzer: starting run")
	ct := time.Now()
	computingStartTime := a.createStartDayTime(ct.Year(), ct.Month(), ct.Day())
	hives, err := a.db.GetHives(a.ctx, "", nil)
	if err != nil {
		a.logger.Error().Err(err).Msg("failed to get hives")
		return
	}
	a.logger.Info().Int("total_hives", len(hives)).Msg("noise analyzer: hives loaded")
	for _, hive := range hives {
		if hive.HubID == nil {
			a.logger.Debug().Int("hiveId", hive.Id).Str("hive", hive.NameHive).Msg("skip hive without hub")
			continue
		}
		SchumeikoDataMap, err := a.db.GetNoiseSinceDay(a.ctx, *hive.HubID, computingStartTime)
		if err != nil {
			a.logger.Warn().Err(err).Int("hiveId", hive.Id).Msg("failed to get noise since time map")
			continue
		}
		a.logger.Info().Int("hiveId", hive.Id).Str("hive", hive.NameHive).Int("days", len(SchumeikoDataMap)).Msg("analyzing noise")
		a.analyzeDay(SchumeikoDataMap, hive, computingStartTime)
		if err := a.db.UpdateHiveNoiseCheck(a.ctx, hive.Id, computingStartTime); err != nil {
			a.logger.Warn().Err(err).Int("hiveId", hive.Id).Msg("failed to update hive noise check")
		}
	}
	a.logger.Info().Msg("noise analyzer: run finished")
}

func (a *Analyzer) createStartDayTime(year int, month time.Month, day int) time.Time {
	return time.Date(year, month, day, 0, 0, 0, 0, time.UTC)
}

// criticalNoiseDelta — порог изменения среднего уровня шума (в dB SPL) между
// двумя соседними днями, при котором поднимается уведомление.
//
// Прошивка в firmware/beeiot_s3/noise.py пишет уровень шума в dB SPL после
// калибровки INMP441 (RMS → dBFS → dB SPL с offset из NOISE_DB_OFFSET).
// Типичный диапазон шума пчелиной семьи — 40…80 dB SPL. Изменение на 8 dB
// соответствует субъективному удвоению громкости и хорошо коррелирует с
// событиями, которые мы реально хотим ловить: начало роения, появление
// нескольких маток, резкое ослабление семьи.
//
// Хотим чувствительнее — снижаем до 5.0; хотим консервативнее — поднимаем
// до 10.0. Старое значение 200.0 было нонсенсом (200 дБ — уровень ударной
// волны, в улье физически недостижим), из-за чего анализатор никогда не
// срабатывал.
const criticalNoiseDelta = 8.0

func (a *Analyzer) analyzeDay(
	data map[time.Time][]dbTypes.HivesNoiseData, hive dbTypes.Hive, curTime time.Time) {

	for date, noises := range data {
		if date.Equal(curTime) {
			continue
		}
		prevTime := time.Date(date.Year(), date.Month(), date.Day(), 0, 0, 0, 0, time.UTC)
		if prevData, ok := data[prevTime.Add(-24*time.Hour)]; ok {
			prev := a.averageNoise(prevData)
			cur := a.averageNoise(noises)
			if math.Abs(prev-cur) < criticalNoiseDelta {
				continue
			}
			a.logger.Info().Int("hiveId", hive.Id).Str("hive", hive.NameHive).Float64("prev", prev).Float64("cur", cur).Msg("abnormal noise detected")
			if a.notification == nil {
				a.logger.Warn().Int("hiveId", hive.Id).Msg("notification service is nil, skipping")
				continue
			}
			tokens, err := a.db.GetFirebaseToken(a.ctx, hive.Email)
			if err != nil {
				a.logger.Warn().Int("hiveId", hive.Id).
					Str("email", hive.Email).Err(err).Msg("failed to get firebase token")
				continue
			}
			a.logger.Info().Int("hiveId", hive.Id).Str("email", hive.Email).Int("tokens", len(tokens)).Msg("sending noise notification")
			if len(tokens) == 0 {
				continue
			}
			badToken, err := a.notification.SendNotification(a.ctx, notification.Data{
				Title: "Критический уровень шума",
				Body: fmt.Sprintf(`Уровень шума изменился с %.2f до %.2f.
Необходимо проверить его состояние`, prev, cur),
				Data: map[string]string{
					"hive": hive.NameHive,
				},
				Tokens:    tokens,
				Important: false,
			})
			switch {
			case errors.Is(err, notification.ErrInvalidTokens):
				err = a.db.DeleteFirebaseToken(a.ctx, hive.Email, badToken)
				if err != nil {
					a.logger.Warn().Int("hiveId", hive.Id).
						Str("email", hive.Email).Err(err).Msg("failed to delete invalid firebase token")
				}
			case err != nil:
				a.logger.Warn().Int("hiveId", hive.Id).
					Str("email", hive.Email).Err(err).Msg("failed to send notification")
			}
		}
	}
}

func (a *Analyzer) averageNoise(data []dbTypes.HivesNoiseData) float64 {
	if len(data) == 0 {
		return 0
	}
	var sum float64
	for _, noise := range data {
		sum += noise.Level
	}
	return sum / float64(len(data))
}
