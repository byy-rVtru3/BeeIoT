package temperature

import (
	"BeeIOT/internal/domain/interfaces"
	"BeeIOT/internal/domain/models/dbTypes"
	"BeeIOT/internal/domain/models/httpType"
	"context"
	"fmt"
	"log/slog"
	"time"
)

type Analyzer struct {
	period  time.Duration
	db      interfaces.DB
	ctx     context.Context
	inMemDb interfaces.InMemoryDB
}

func NewAnalyzer(ctx context.Context, period time.Duration, db interfaces.DB, inMemDb interfaces.InMemoryDB) *Analyzer {
	return &Analyzer{period: period, db: db, ctx: ctx, inMemDb: inMemDb}
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
		slog.Error("не удалось получить улья для анализа температуры",
			"module", "temperature", "function", "analyzeTemperature", "error", err)
		return
	}
	for _, hive := range hives {
		data, err := a.db.GetTemperaturesSinceTimeById(a.ctx, hive.Id, hive.DateTemperature)
		if err != nil {
			slog.Warn("не удалось получить температуру для улья",
				"module", "temperature", "function", "analyzeTemperature", "id", hive.Id, "error", err)
			continue
		}
		a.temperatureAnalysis(data, hive)
		hive.DateTemperature = time.Now()
		if a.db.UpdateHive(a.ctx, hive.NameHive, hive) != nil {
			slog.Warn("не удалось обновить дату последней проверки температуры",
				"module", "temperature", "function", "analyzeTemperature", "id", hive.Id)
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
		if !a.isNormallyTemperature(elem.Temperature) {
			email, err := a.db.GetUserById(a.ctx, hive.Id)
			if err != nil {
				slog.Warn("не удалось получить email пользователя",
					"module", "temperature", "function", "analyzeTemperature",
					"id", hive.Id, "email", email, "error", err)
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
				slog.Warn("не удалось сохранить уведомление об отклонении температуры",
					"module", "temperature", "function", "analyzeTemperature",
					"id", hive.Id, "email", email, "error", err)
			}
		}
	}
}
