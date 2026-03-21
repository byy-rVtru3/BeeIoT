package postgres

import (
	"BeeIOT/internal/domain/models/dbTypes"
	"BeeIOT/internal/domain/models/httpType"
	"context"
	"time"
)

func (db *Postgres) NewNoise(ctx context.Context, noise httpType.NoiseLevel) error {
	text := `INSERT INTO noise (hub_id, level, recorded_at)
             SELECT id, $3, $4
             FROM hubs
             WHERE email = $1 AND sensor = $2
             ON CONFLICT (hub_id, recorded_at) DO NOTHING;`
	_, err := db.pull.Exec(ctx, text, noise.Email, noise.Hub, noise.Level, noise.Time)
	return err
}

func (db *Postgres) GetNoiseSinceTime(ctx context.Context, email, hub string, t time.Time) ([]dbTypes.HivesNoiseData, error) {
	text := `SELECT level, recorded_at FROM noise n
             INNER JOIN hubs h ON n.hub_id = h.id
	         WHERE h.email = $1 AND h.sensor = $2 AND n.recorded_at >= $3;`
	rows, err := db.pull.Query(ctx, text, email, hub, t)
	if err != nil {
		return nil, err
	}
	defer rows.Close()
	var noiseLevels []dbTypes.HivesNoiseData
	for rows.Next() {
		var n dbTypes.HivesNoiseData
		if err := rows.Scan(&n.Level, &n.Date); err != nil {
			return nil, err
		}
		noiseLevels = append(noiseLevels, n)
	}
	return noiseLevels, nil
}

func (db *Postgres) GetNoiseSinceDay(ctx context.Context, hubId int, date time.Time) (map[time.Time][]dbTypes.HivesNoiseData, error) {
	text := `SELECT level, recorded_at FROM noise
			 WHERE hub_id = $1 AND recorded_at >= $2;`
	rows, err := db.pull.Query(ctx, text, hubId, date)
	if err != nil {
		return nil, err
	}
	defer rows.Close()
	noiseDataMap := make(map[time.Time][]dbTypes.HivesNoiseData)
	for rows.Next() {
		var noiseData dbTypes.HivesNoiseData
		if err := rows.Scan(&noiseData.Level, &noiseData.Date); err != nil {
			return nil, err
		}
		day := db.createStartDayTime(noiseData.Date.Year(), noiseData.Date.Month(), noiseData.Date.Day())
		noiseDataMap[day] = append(noiseDataMap[day], noiseData)
	}
	return noiseDataMap, nil
}

func (db *Postgres) createStartDayTime(year int, month time.Month, day int) time.Time {
	return time.Date(year, month, day, 0, 0, 0, 0, time.UTC)
}
