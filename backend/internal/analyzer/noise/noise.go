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
	ct := time.Now()
	computingStartTime := a.createStartDayTime(ct.Year(), ct.Month(), ct.Day())
	hives, err := a.db.GetHives(a.ctx, "", nil)
	if err != nil {
		a.logger.Error().Err(err).Msg("failed to get hives")
		return
	}
	for _, hive := range hives {
		if hive.HubID == nil {
			continue
		}
		SchumeikoDataMap, err := a.db.GetNoiseSinceDay(a.ctx, *hive.HubID, computingStartTime)
		if err != nil {
			a.logger.Warn().Err(err).Int("hiveId", hive.Id).Msg("failed to get noise since time map")
			continue
		}
		a.analyzeDay(SchumeikoDataMap, hive, computingStartTime)
		if err := a.db.UpdateHiveNoiseCheck(a.ctx, hive.Id, computingStartTime); err != nil {
			a.logger.Warn().Err(err).Int("hiveId", hive.Id).Msg("failed to update hive noise check")
		}
	}
}

func (a *Analyzer) createStartDayTime(year int, month time.Month, day int) time.Time {
	return time.Date(year, month, day, 0, 0, 0, 0, time.UTC)
}

const criticalNoiseDelta = 200.0

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
