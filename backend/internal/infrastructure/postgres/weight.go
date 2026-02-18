package postgres

import (
	"BeeIOT/internal/domain/models/httpType"
	"context"
	"time"
)

func (db *Postgres) NewHiveWeight(ctx context.Context, weight httpType.HiveWeight) error {
	text := `INSERT INTO weight (hive_id, level, recorded_at)
             SELECT h.id, $1, $2
             FROM hives h
             INNER JOIN users u ON h.user_id = u.id
			 WHERE u.email = $3 AND h.name = $4;`
	_, err := db.pull.Exec(ctx, text, weight.Weight, weight.Time, weight.Email, weight.Hive)
	return err
}

func (db *Postgres) DeleteHiveWeight(ctx context.Context, weight httpType.HiveWeight) error {
	text := `DELETE FROM weight w
             USING hives h
			 INNER JOIN users u ON h.user_id = u.id
			 WHERE w.hive_id = h.id AND u.email = $1 AND h.name = $2 AND w.recorded_at = $3;`
	_, err := db.pull.Exec(ctx, text, weight.Email, weight.Hive, weight.Time)
	return err
}

func (db *Postgres) getWeightSinceTime(ctx context.Context, hive httpType.Hive, time time.Time) ([]httpType.HiveWeight, error) {
	text := `SELECT level, recorded_at 
             FROM weight w
             INNER JOIN hives h ON h.id = w.hive_id
             INNER JOIN users u ON h.user_id = u.id
             WHERE h.name = $2 AND u.email = $1 AND w.recorded_at >= $3;`
	rows, err := db.pull.Query(ctx, text, hive.Email, hive.NameHive, time)
	if err != nil {
		return nil, err
	}
	defer rows.Close()
	var weights []httpType.HiveWeight
	for rows.Next() {
		var weight httpType.HiveWeight
		err := rows.Scan(&weight.Weight, &weight.Time)
		if err != nil {
			return nil, err
		}
		weights = append(weights, weight)
	}
	return weights, nil
}
