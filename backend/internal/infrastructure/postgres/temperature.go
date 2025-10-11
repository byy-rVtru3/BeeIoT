package postgres

import (
	"BeeIoT/backend/internal/domain/types/http"
	"context"
	"time"
)

func (db *Postgres) NewTemperature(ctx context.Context, temp http.Temperature) error {
	text := `INSERT INTO temperature_hive (user_id, hive_id, temperature, time) 
			 VALUES (
				(SELECT id FROM users WHERE email = $1 AND SUBSTRING(password, 1, 10) = $2),
				(SELECT id FROM hives WHERE name = $3 AND user_id = (SELECT id FROM users WHERE email = $1 AND SUBSTRING(password, 1, 10) = $2)),
				$4, $5
			 );`
	_, err := db.conn.Exec(ctx, text, temp.Email, temp.Hash, temp.Hive, temp.Temperature, temp.Time)
	return err
}

func (db *Postgres) DeleteTemperature(ctx context.Context, temp http.Temperature) error {
	text := `DELETE FROM temperature_hive 
			 WHERE user_id = (SELECT id FROM users WHERE email = $1 AND SUBSTRING(password, 1, 10) = $2)
			 AND hive_id = (SELECT id FROM hives WHERE name = $3 AND user_id = (SELECT id FROM users WHERE email = $1 AND SUBSTRING(password, 1, 10) = $2))
			 AND time = $4;`
	_, err := db.conn.Exec(ctx, text, temp.Email, temp.Hash, temp.Hive, temp.Time)
	return err
}

func (db *Postgres) getTemperaturesSinceTime(ctx context.Context, hive http.Hive, time time.Time) ([]http.Temperature, error) {
	text := `SELECT temperature, time FROM temperature_hive 
			 WHERE user_id = (SELECT id FROM users WHERE email = $1 AND SUBSTRING(password, 1, 10) = $2)
			 AND hive_id = (SELECT id FROM hives WHERE name = $3 AND user_id = (SELECT id FROM users WHERE email = $1 AND SUBSTRING(password, 1, 10) = $2))
			 AND time >= $4;`
	rows, err := db.conn.Query(ctx, text, hive.Email, hive.Hash, hive.NameHive, time)
	if err != nil {
		return nil, err
	}
	defer rows.Close()
	var temperatures []http.Temperature
	for rows.Next() {
		var temp http.Temperature
		err := rows.Scan(&temp.Temperature, &temp.Time)
		if err != nil {
			return nil, err
		}
		temperatures = append(temperatures, temp)
	}
	return temperatures, nil
}

func (db *Postgres) GetTemperatureForDay(ctx context.Context, hive http.Hive) ([]http.Temperature, error) {
	dayAgo := time.Now().Add(-24 * time.Hour)
	return db.getTemperaturesSinceTime(ctx, hive, dayAgo)
}

func (db *Postgres) GetTemperatureForWeek(ctx context.Context, hive http.Hive) ([]http.Temperature, error) {
	weekAgo := time.Now().Add(-7 * 24 * time.Hour)
	return db.getTemperaturesSinceTime(ctx, hive, weekAgo)
}

func (db *Postgres) GetTemperatureForMonth(ctx context.Context, hive http.Hive) ([]http.Temperature, error) {
	monthAgo := time.Now().Add(-30 * 24 * time.Hour)
	return db.getTemperaturesSinceTime(ctx, hive, monthAgo)
}
