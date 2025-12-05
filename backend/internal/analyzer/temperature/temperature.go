package temperature

import (
	"BeeIOT/internal/domain/interfaces"
	"BeeIOT/internal/domain/models/dbTypes"
	"BeeIOT/internal/domain/models/httpType"
	"context"
	"fmt"
	"time"

	"github.com/rs/zerolog"
)

type Analyzer struct {
	period  time.Duration
	db      interfaces.DB
	ctx     context.Context
	inMemDb interfaces.InMemoryDB
	logger  zerolog.Logger
}

func NewAnalyzer(ctx context.Context, period time.Duration, db interfaces.DB, inMemDb interfaces.InMemoryDB) *Analyzer {
	logger := ctx.Value("logger").(zerolog.Logger)
	return &Analyzer{period: period, db: db, ctx: ctx, inMemDb: inMemDb, logger: logger}
}

func (a *Analyzer) Start() {
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
}

func (a *Analyzer) analyzeTemperature() {
	hives, err := a.db.GetHives(a.ctx, "")
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
		hive.DateTemperature = time.Now()
		if a.db.UpdateHive(a.ctx, hive.NameHive, hive) != nil {
			a.logger.Warn().Err(err).Int("hiveId", hive.Id).Msg("failed to update hive")
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
		email, err := a.db.GetUserById(a.ctx, hive.Id)
		if err != nil {
			a.logger.Warn().Err(err).Int("hiveId", hive.Id).Str("email", email).Msg("failed to get user")
			continue
		}
		err = a.inMemDb.SetNotification(a.ctx, email, httpType.NotificationData{
			Text: fmt.Sprintf(`Обнаружено отклонение температуры в улье %s: %.2f°C.
Нормальное значение температуры находится в пределе от %.2f до %.2f.
Необходимо принять меры`, hive.NameHive, elem.Temperature,
				temperatureNormal-temperatureDeltaDown, temperatureNormal+temperatureDeltaUp),
			NameHive: hive.NameHive,
			Date:     elem.Date.UnixNano(),
		})
		if err != nil {
			a.logger.Warn().Err(err).Int("hiveId", hive.Id).Str("email", email).Msg("failed to set notification")
		}
	}
}
