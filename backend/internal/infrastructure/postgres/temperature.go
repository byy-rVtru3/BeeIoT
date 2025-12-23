package postgres

import (
	"BeeIOT/internal/domain/models/dbTypes"
	"BeeIOT/internal/domain/models/httpType"
	"context"
	"time"
)

func (db *Postgres) NewTemperature(ctx context.Context, temp httpType.Temperature) error {
	text := `INSERT INTO temperature (hive_id, level, recorded_at) 
			 VALUES (
				(SELECT id FROM hives WHERE name = $2 AND user_id = (SELECT id FROM users WHERE email = $1)),
				$3, $4
			 );`
	_, err := db.conn.Exec(ctx, text, temp.Email, temp.Hive, temp.Temperature, temp.Time)
	return err
}

func (db *Postgres) DeleteTemperature(ctx context.Context, temp httpType.Temperature) error {
	text := `DELETE FROM temperature 
			 WHERE hive_id = (SELECT id FROM hives WHERE name = $2 AND user_id = (SELECT id FROM users WHERE email = $1))
			 AND recorded_at = $3;`
	_, err := db.conn.Exec(ctx, text, temp.Email, temp.Hive, temp.Time)
	return err
}

func (db *Postgres) GetTemperaturesSinceTime(ctx context.Context, hive dbTypes.Hive, time time.Time) ([]dbTypes.HivesTemperatureData, error) {
	text := `SELECT level, recorded_at FROM temperature 
			 WHERE hive_id = (SELECT id FROM hives WHERE name = $2 AND user_id = (SELECT id FROM users WHERE email = $1))
			 AND recorded_at >= $3;`
	rows, err := db.conn.Query(ctx, text, hive.Email, hive.NameHive, time)
	if err != nil {
		return nil, err
	}
	defer rows.Close()
	var temperatures []dbTypes.HivesTemperatureData
	for rows.Next() {
		var temp dbTypes.HivesTemperatureData
		err := rows.Scan(&temp.Temperature, &temp.Date)
		if err != nil {
			return nil, err
		}
		temperatures = append(temperatures, temp)
	}
	return temperatures, nil
}

func (db *Postgres) GetTemperaturesSinceTimeById(ctx context.Context, hiveId int, time time.Time) ([]dbTypes.HivesTemperatureData, error) {
	text := `SELECT level, recorded_at FROM temperature
            WHERE hive_id = $1
			AND recorded_at >= $2;`
	rows, err := db.conn.Query(ctx, text, hiveId, time)
	if err != nil {
		return nil, err
	}
	defer rows.Close()
	var temperatures []dbTypes.HivesTemperatureData
	for rows.Next() {
		var temp dbTypes.HivesTemperatureData
		err := rows.Scan(&temp.Temperature, &temp.Date)
		if err != nil {
			return nil, err
		}
		temperatures = append(temperatures, temp)
	}
	return temperatures, nil
}
