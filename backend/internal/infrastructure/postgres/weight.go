package postgres

import (
	"BeeIOT/internal/domain/models/httpType"
	"context"
	"time"
)

func (db *Postgres) NewHiveWeight(ctx context.Context, weight httpType.HiveWeight) error {
	text := `INSERT INTO weight_hive (user_id, hive_id, weight, time) 
			 VALUES (
				(SELECT id FROM users WHERE email = $1 AND SUBSTRING(password, 1, 10) = $2),
				(SELECT id FROM hives WHERE name = $3 AND user_id = (SELECT id FROM users WHERE email = $1 AND SUBSTRING(password, 1, 10) = $2)),
				$4, $5
			 );`
	_, err := db.conn.Exec(ctx, text, weight.Email, weight.Hash, weight.Hive, weight.Weight, weight.Time)
	return err
}

func (db *Postgres) DeleteHiveWeight(ctx context.Context, weight httpType.HiveWeight) error {
	text := `DELETE FROM weight_hive 
			 WHERE user_id = (SELECT id FROM users WHERE email = $1 AND SUBSTRING(password, 1, 10) = $2)
			 AND hive_id = (SELECT id FROM hives WHERE name = $3 AND user_id = (SELECT id FROM users WHERE email = $1 AND SUBSTRING(password, 1, 10) = $2))
			 AND time = $4;`
	_, err := db.conn.Exec(ctx, text, weight.Email, weight.Hash, weight.Hive, weight.Time)
	return err
}

func (db *Postgres) getHiveWeightsSinceTime(ctx context.Context, hive httpType.Hive, time time.Time) ([]httpType.HiveWeight, error) {
	text := `SELECT weight, time FROM weight_hive 
			 WHERE user_id = (SELECT id FROM users WHERE email = $1 AND SUBSTRING(password, 1, 10) = $2)
			 AND hive_id = (SELECT id FROM hives WHERE name = $3 AND user_id = (SELECT id FROM users WHERE email = $1 AND SUBSTRING(password, 1, 10) = $2))
			 AND time >= $4;`
	rows, err := db.conn.Query(ctx, text, hive.Email, hive.Hash, hive.NameHive, time)
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

func (db *Postgres) GetHiveWeightForDay(ctx context.Context, hive httpType.Hive) ([]httpType.HiveWeight, error) {
	dayAgo := time.Now().Add(-24 * time.Hour)
	return db.getHiveWeightsSinceTime(ctx, hive, dayAgo)
}

func (db *Postgres) GetHiveWeightForWeek(ctx context.Context, hive httpType.Hive) ([]httpType.HiveWeight, error) {
	weekAgo := time.Now().Add(-7 * 24 * time.Hour)
	return db.getHiveWeightsSinceTime(ctx, hive, weekAgo)
}

func (db *Postgres) GetHiveWeightForMonth(ctx context.Context, hive httpType.Hive) ([]httpType.HiveWeight, error) {
	monthAgo := time.Now().Add(-30 * 24 * time.Hour)
	return db.getHiveWeightsSinceTime(ctx, hive, monthAgo)
}
