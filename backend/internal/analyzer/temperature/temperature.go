package temperature

import (
	"BeeIOT/internal/domain/interfaces"
	"BeeIOT/internal/domain/models/dbTypes"
	"BeeIOT/internal/domain/notification"
	"context"
	"errors"
	"fmt"
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
	return &Analyzer{period: period, db: db, ctx: ctx, logger: logger, notification: notification}
}

func (a *Analyzer) Start() {
	go func() {
		ticker := time.NewTicker(a.period)
		defer ticker.Stop()
		for {
			select {
			case <-ticker.C:
				a.analyzeTemperature()
			case <-a.ctx.Done():
				return
			}
		}
	}()
}

func (a *Analyzer) analyzeTemperature() {
	hives, err := a.db.GetHives(a.ctx, "", nil)
	if err != nil {
		a.logger.Error().Err(err).Msg("failed to get hives")
		return
	}
	for _, hive := range hives {
		data, err := a.db.GetTemperaturesSinceTimeById(a.ctx, hive.Id, hive.DateTemperature)
		if err != nil {
			a.logger.Warn().Err(err).Int("hiveId", hive.Id).Msg("failed to get temperature")
			continue
		}
		a.temperatureAnalysis(data, hive)
		if errUpd := a.db.UpdateHiveTemperatureCheck(a.ctx, hive.Id, time.Now()); errUpd != nil {
			a.logger.Warn().Err(errUpd).Int("hiveId", hive.Id).Msg("failed to update hive temperature check")
		}
	}
}

const temperatureNormal = 34.0
const temperatureDeltaUp = 5.0
const temperatureDeltaDown = 5.0

func (a *Analyzer) isNormallyTemperature(temp float64) bool {
	return temp >= (temperatureNormal-temperatureDeltaDown) && temp <= (temperatureNormal+temperatureDeltaUp)
}

func (a *Analyzer) temperatureAnalysis(data []dbTypes.HivesTemperatureData, hive dbTypes.Hive) {
	for _, elem := range data {
		if a.isNormallyTemperature(elem.Temperature) {
			continue
		}
		if a.notification == nil {
			a.logger.Warn().Int("hiveId", hive.Id).Msg("notification service is nil, skipping")
			continue
		}
		tokens, err := a.db.GetFirebaseToken(a.ctx, hive.Email)
		if err != nil {
			a.logger.Warn().Err(err).Int("hiveId", hive.Id).Str("email", hive.Email).Msg("failed to get firebase tokens")
			continue
		}
		badToken, err := a.notification.SendNotification(a.ctx, notification.Data{
			Title: "Критический уровень температуры в улье",
			Body: fmt.Sprintf(`Текущее значение температуры сейчас: %.2f.
Норма: %.2f +- %.2f. Необходимо проверить состояние улья`, elem.Temperature, temperatureNormal, temperatureDeltaUp),
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
