package noise

import (
	"BeeIOT/internal/domain/interfaces"
	"BeeIOT/internal/domain/models/dbTypes"
	"BeeIOT/internal/domain/models/httpType"
	"context"
	"fmt"
	"log/slog"
	"math"
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
			a.analyzeNoise()
		case <-a.ctx.Done():
			return
		}
	}
}

func (a *Analyzer) analyzeNoise() {
	ct := time.Now()
	computingStartTime := a.createStartDayTime(ct.Year(), ct.Month(), ct.Day())
	hives, err := a.db.GetHives(a.ctx, "")
	if err != nil {
		slog.Error("не удалось получить улья для анализа шума",
			"module", "temperature", "function", "analyzeTemperature", "error", err)
		return
	}
	for _, hive := range hives {
		SchumeikoDataMap, err := a.db.GetNoiseSinceTimeMap(a.ctx, hive.Id, computingStartTime)
		if err != nil {
			slog.Warn("не удалось получить данные по шуму для анализа",
				"module", "noise", "function", "analyzeNoise", "hiveId", hive.Id, "error", err)
			continue
		}
		a.analyzeDay(SchumeikoDataMap, hive, computingStartTime)
		hive.DateNoise = computingStartTime
	}
}

func (a *Analyzer) createStartDayTime(year int, month time.Month, day int) time.Time {
	return time.Date(year, month, day, 0, 0, 0, 0, time.UTC)
}

const criticalNoiseDelta = 200.0

func (a *Analyzer) analyzeDay(
	data map[time.Time][]dbTypes.HivesNoiseData, hive dbTypes.Hive, curTime time.Time) {

	for date, noises := range data {
		if date == curTime {
			continue
		}
		prevTime := time.Date(date.Year(), date.Month(), date.Day(), 0, 0, 0, 0, time.UTC)
		if prevData, ok := data[prevTime.Add(-24*time.Hour)]; ok {
			prev := a.averageNoise(prevData)
			cur := a.averageNoise(noises)
			if math.Abs(prev-cur) >= criticalNoiseDelta {
				email, err := a.db.GetUserById(a.ctx, hive.Id)
				if err != nil {
					slog.Warn("не удалось получить email пользователя",
						"module", "temperature", "function", "analyzeTemperature",
						"id", hive.Id, "email", email, "error", err)
					continue
				}
				err = a.inMemDb.SetNotification(a.ctx, email, httpType.NotificationData{
					Text: fmt.Sprintf(`Обнаружено отклонение шума в улье %s.
За день уровень шума изменился с %.2f до %.2f.
Необходимо принять меры.`, hive.NameHive, prev, cur),
					NameHive: hive.NameHive,
					Date:     date.UnixNano(),
				})
				if err != nil {
					slog.Warn("не удалось сохранить уведомление об отклонении температуры",
						"module", "temperature", "function", "analyzeTemperature",
						"id", hive.Id, "email", email, "error", err)
				}
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
