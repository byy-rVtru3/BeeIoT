package postgres

import (
	"BeeIOT/internal/domain/types/httpType"
	"context"
	"time"

	"github.com/google/uuid"
)

func (db *Postgres) NewTemperature(ctx context.Context, temp httpType.Temperature) error {
	text := `INSERT INTO temperature_hive (user_id, hive_id, temperature, time) 
			 VALUES (
				(SELECT id FROM users WHERE email = $1),
				(SELECT id FROM hives WHERE name = $2 AND user_id = (SELECT id FROM users WHERE email = $1),
				$3, $4
			 );`
	_, err := db.conn.Exec(ctx, text, temp.Email, temp.Hive, temp.Temperature, temp.Time)
	return err
}

func (db *Postgres) DeleteTemperature(ctx context.Context, temp httpType.Temperature) error {
	text := `DELETE FROM temperature_hive 
			 WHERE user_id = (SELECT id FROM users WHERE email = $1)
			 AND hive_id = (SELECT id FROM hives WHERE name = $2 AND user_id = (SELECT id FROM users WHERE email = $1)))
			 AND time = $3;`
	_, err := db.conn.Exec(ctx, text, temp.Email, temp.Hive, temp.Time)
	return err
}

func (db *Postgres) GetTemperaturesSinceTime(ctx context.Context, hive httpType.Hive, time time.Time) ([]httpType.Temperature, error) {
	text := `SELECT temperature, time FROM temperature_hive 
			 WHERE user_id = (SELECT id FROM users WHERE email = $1)
			 AND hive_id = (SELECT id FROM hives WHERE name = $2 AND user_id = (SELECT id FROM users WHERE email = $1))
			 AND time >= $3;`
	rows, err := db.conn.Query(ctx, text, hive.Email, hive.NameHive, time)
	if err != nil {
		return nil, err
	}
	defer rows.Close()
	var temperatures []httpType.Temperature
	for rows.Next() {
		var temp httpType.Temperature
		err := rows.Scan(&temp.Temperature, &temp.Time)
		if err != nil {
			return nil, err
		}
		temperatures = append(temperatures, temp)
	}
	return temperatures, nil
}

func (db *Postgres) GetTemperaturesSinceTimeById(ctx context.Context, hiveId int, time time.Time) ([]httpType.HivesTemperatureData, error) {

}
