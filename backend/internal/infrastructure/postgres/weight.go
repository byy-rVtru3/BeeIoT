package postgres

import (
	"BeeIOT/internal/domain/models/dbTypes"
	"BeeIOT/internal/domain/models/httpType"
	"context"
	"time"
)

func (db *Postgres) NewHiveWeight(ctx context.Context, weight httpType.HubWeight) error {
	text := `INSERT INTO weight (hub_id, level, recorded_at)
             SELECT id, $1, $2
             FROM hubs
			 WHERE email = $3 AND sensor = $4
			 ON CONFLICT (hub_id, recorded_at) DO NOTHING;`
	_, err := db.pull.Exec(ctx, text, weight.Weight, weight.Time, weight.Email, weight.Hub)
	return err
}

func (db *Postgres) DeleteHiveWeight(ctx context.Context, weight httpType.HubWeight) error {
	text := `DELETE FROM weight w
             USING hubs h
             WHERE w.hub_id = h.id
              AND h.email = $1
              AND h.sensor = $2
              AND w.recorded_at = $3;`
	_, err := db.pull.Exec(ctx, text, weight.Email, weight.Hub, weight.Time)
	return err
}

func (db *Postgres) GetWeightSinceTime(ctx context.Context, email, hub string, t time.Time) ([]dbTypes.HivesWeightData, error) {
	text := `SELECT level, recorded_at
             FROM weight w
             INNER JOIN hubs h ON h.id = w.hub_id
             WHERE h.email = $1 AND h.sensor = $2 AND w.recorded_at >= $3;`
	rows, err := db.pull.Query(ctx, text, email, hub, t)
	if err != nil {
		return nil, err
	}
	defer rows.Close()
	var weights []dbTypes.HivesWeightData
	for rows.Next() {
		var weight dbTypes.HivesWeightData
		if err := rows.Scan(&weight.Weight, &weight.Date); err != nil {
			return nil, err
		}
		weights = append(weights, weight)
	}
	return weights, nil
}
