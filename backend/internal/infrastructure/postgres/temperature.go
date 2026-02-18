package postgres

import (
	"BeeIOT/internal/domain/models/dbTypes"
	"BeeIOT/internal/domain/models/httpType"
	"context"
	"time"
)

func (db *Postgres) NewTemperature(ctx context.Context, temp httpType.Temperature) error {
	text := `INSERT INTO temperature (hive_id, level, recorded_at)
             SELECT id, $3, $4
             FROM hives h
             INNER JOIN users u ON h.user_id = u.id
             WHERE u.email = $1 AND h.name = $2;`
	_, err := db.pull.Exec(ctx, text, temp.Email, temp.Hive, temp.Temperature, temp.Time)
	return err
}

func (db *Postgres) GetTemperaturesSinceTime(ctx context.Context, hive dbTypes.Hive, time time.Time) ([]dbTypes.HivesTemperatureData, error) {
	text := `SELECT level, recorded_at FROM temperature n
             INNER JOIN hives h ON n.hive_id = h.id
             INNER JOIN users u ON h.user_id = u.id
             WHERE h.name = $2 AND u.email = $1 AND n.recorded_at >= $3;`
	rows, err := db.pull.Query(ctx, text, hive.Email, hive.NameHive, time)
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
	rows, err := db.pull.Query(ctx, text, hiveId, time)
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
