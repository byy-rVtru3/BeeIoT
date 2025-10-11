package postgres

import (
	"BeeIoT/backend/internal/domain/types/http"
	"context"
	"time"
)

func (db *Postgres) NewNoiseLevel(ctx context.Context, noise http.NoiseLevel) error {
	text := `INSERT INTO noise_hive (user_id, hive_id, level, time) 
			 VALUES (
				(SELECT id FROM users WHERE email = $1 AND SUBSTRING(password, 1, 10) = $2),
				(SELECT id FROM hives WHERE name = $3 AND user_id = (SELECT id FROM users WHERE email = $1 AND SUBSTRING(password, 1, 10) = $2)),
				$4, $5
			 );`
	_, err := db.conn.Exec(ctx, text, noise.Email, noise.Hash, noise.Hive, noise.Level, noise.Time)
	return err
}

func (db *Postgres) DeleteNoiseLevel(ctx context.Context, noise http.NoiseLevel) error {
	text := `DELETE FROM noise_hive 
			 WHERE user_id = (SELECT id FROM users WHERE email = $1 AND SUBSTRING(password, 1, 10) = $2)
			 AND hive_id = (SELECT id FROM hives WHERE name = $3 AND user_id = (SELECT id FROM users WHERE email = $1 AND SUBSTRING(password, 1, 10) = $2))
			 AND time = $4;`
	_, err := db.conn.Exec(ctx, text, noise.Email, noise.Hash, noise.Hive, noise.Time)
	return err
}

func (db *Postgres) getNoiseLevelsSinceTime(ctx context.Context, hive http.Hive, time time.Time) ([]http.NoiseLevel, error) {
	text := `SELECT level, time FROM noise_hive 
			 WHERE user_id = (SELECT id FROM users WHERE email = $1 AND SUBSTRING(password, 1, 10) = $2)
			 AND hive_id = (SELECT id FROM hives WHERE name = $3 AND user_id = (SELECT id FROM users WHERE email = $1 AND SUBSTRING(password, 1, 10) = $2))
			 AND time >= $4;`
	rows, err := db.conn.Query(ctx, text, hive.Email, hive.Hash, hive.NameHive, time)
	if err != nil {
		return nil, err
	}
	defer rows.Close()
	var noiseLevels []http.NoiseLevel
	for rows.Next() {
		var noise http.NoiseLevel
		err := rows.Scan(&noise.Level, &noise.Time)
		if err != nil {
			return nil, err
		}
		noiseLevels = append(noiseLevels, noise)
	}
	return noiseLevels, nil
}

func (db *Postgres) GetNoiseLevelForDay(ctx context.Context, hive http.Hive) ([]http.NoiseLevel, error) {
	dayAgo := time.Now().Add(-24 * time.Hour)
	return db.getNoiseLevelsSinceTime(ctx, hive, dayAgo)
}

func (db *Postgres) GetNoiseLevelForWeek(ctx context.Context, hive http.Hive) ([]http.NoiseLevel, error) {
	weekAgo := time.Now().Add(-7 * 24 * time.Hour)
	return db.getNoiseLevelsSinceTime(ctx, hive, weekAgo)
}

func (db *Postgres) GetNoiseLevelForMonth(ctx context.Context, hive http.Hive) ([]http.NoiseLevel, error) {
	monthAgo := time.Now().Add(-30 * 24 * time.Hour)
	return db.getNoiseLevelsSinceTime(ctx, hive, monthAgo)
}
