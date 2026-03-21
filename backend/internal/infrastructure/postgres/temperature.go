package postgres

import (
	"BeeIOT/internal/domain/models/dbTypes"
	"BeeIOT/internal/domain/models/httpType"
	"context"
	"time"
)

func (db *Postgres) NewTemperature(ctx context.Context, temp httpType.Temperature) error {
	text := `INSERT INTO temperature (hub_id, level, recorded_at)
             SELECT id, $3, $4
             FROM hubs
             WHERE email = $1 AND sensor = $2
             ON CONFLICT (hub_id, recorded_at) DO NOTHING;`
	_, err := db.pull.Exec(ctx, text, temp.Email, temp.Hub, temp.Temperature, temp.Time)
	return err
}

func (db *Postgres) GetTemperaturesSinceTime(ctx context.Context, email, hub string, t time.Time) ([]dbTypes.HivesTemperatureData, error) {
	text := `SELECT level, recorded_at FROM temperature t
             INNER JOIN hubs h ON t.hub_id = h.id
             WHERE h.email = $1 AND h.sensor = $2 AND t.recorded_at >= $3;`
	rows, err := db.pull.Query(ctx, text, email, hub, t)
	if err != nil {
		return nil, err
	}
	defer rows.Close()
	var temperatures []dbTypes.HivesTemperatureData
	for rows.Next() {
		var temp dbTypes.HivesTemperatureData
		if err := rows.Scan(&temp.Temperature, &temp.Date); err != nil {
			return nil, err
		}
		temperatures = append(temperatures, temp)
	}
	return temperatures, nil
}

func (db *Postgres) GetTemperaturesSinceTimeById(ctx context.Context, hubId int, t time.Time) ([]dbTypes.HivesTemperatureData, error) {
	text := `SELECT level, recorded_at FROM temperature
             WHERE hub_id = $1
			 AND recorded_at >= $2;`
	rows, err := db.pull.Query(ctx, text, hubId, t)
	if err != nil {
		return nil, err
	}
	defer rows.Close()
	var temperatures []dbTypes.HivesTemperatureData
	for rows.Next() {
		var temp dbTypes.HivesTemperatureData
		if err := rows.Scan(&temp.Temperature, &temp.Date); err != nil {
			return nil, err
		}
		temperatures = append(temperatures, temp)
	}
	return temperatures, nil
}
