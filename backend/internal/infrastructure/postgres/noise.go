package postgres

import (
	"BeeIOT/internal/domain/models/dbTypes"
	"BeeIOT/internal/domain/models/httpType"
	"context"
	"time"
)

func (db *Postgres) NewNoise(ctx context.Context, noise httpType.NoiseLevel) error {
	text := `INSERT INTO noise (hive_id, level, recorded_at) 
			 VALUES (
				(SELECT id FROM hives WHERE name = $2 AND user_id = (SELECT id FROM users WHERE email = $1)),
				$3, $4
			 );`
	_, err := db.conn.Exec(ctx, text, noise.Email, noise.Hive, noise.Level, noise.Time)
	return err
}

func (db *Postgres) DeleteNoise(ctx context.Context, noise httpType.NoiseLevel) error {
	text := `DELETE FROM noise 
			 WHERE hive_id = (SELECT id FROM hives WHERE name = $2 AND user_id = (SELECT id FROM users WHERE email = $1))
			 AND recorded_at = $3;`
	_, err := db.conn.Exec(ctx, text, noise.Email, noise.Hive, noise.Time)
	return err
}

func (db *Postgres) GetNoiseSinceTime(
	ctx context.Context, email, nameHive string, time time.Time) ([]httpType.NoiseLevel, error) {
	text := `SELECT level, recorded_at FROM noise 
			 WHERE hive_id = (SELECT id FROM hives WHERE name = $2 AND user_id = (SELECT id FROM users WHERE email = $1))
			 AND recorded_at >= $3;`
	rows, err := db.conn.Query(ctx, text, email, nameHive, time)
	if err != nil {
		return nil, err
	}
	defer rows.Close()
	var noiseLevels []httpType.NoiseLevel
	for rows.Next() {
		var noise httpType.NoiseLevel
		err := rows.Scan(&noise.Level, &noise.Time)
		if err != nil {
			return nil, err
		}
		noiseLevels = append(noiseLevels, noise)
	}
	return noiseLevels, nil
}

func (db *Postgres) GetNoiseSinceTimeMap(
	ctx context.Context, id int, date time.Time) (map[time.Time][]dbTypes.HivesNoiseData, error) {
	text := `SELECT level, recorded_at FROM noise
			 WHERE hive_id = $1
			 AND recorded_at >= $2;`
	rows, err := db.conn.Query(ctx, text, id, date)
	if err != nil {
		return nil, err
	}
	defer rows.Close()
	noiseDataMap := make(map[time.Time][]dbTypes.HivesNoiseData)
	for rows.Next() {
		var noiseData dbTypes.HivesNoiseData
		err := rows.Scan(&noiseData.Level, &noiseData.Date)
		if err != nil {
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
