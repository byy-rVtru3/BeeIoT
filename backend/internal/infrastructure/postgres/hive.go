package postgres

import (
	"BeeIOT/internal/domain/types/httpType"
	"context"
)

func (db *Postgres) NewHive(ctx context.Context, hive httpType.Hive) error {
	text := `INSERT INTO hives (user_id, name) 
			 VALUES ((SELECT id FROM users WHERE email = $1 AND SUBSTRING(password, 1, 10) = $2), $3);`
	_, err := db.conn.Exec(ctx, text, hive.Email, hive.Hash, hive.NameHive)
	return err
}

func (db *Postgres) DeleteHive(ctx context.Context, hive httpType.Hive) error {
	text := `DELETE FROM hives 
			 WHERE user_id = (SELECT id FROM users WHERE email = $1 AND SUBSTRING(password, 1, 10) = $2) 
			 AND name = $3;`
	_, err := db.conn.Exec(ctx, text, hive.Email, hive.Hash, hive.NameHive)
	return err
}

func (db *Postgres) GetHives(ctx context.Context, req httpType.Hive) ([]string, error) {
	text := `SELECT name FROM hives 
			 WHERE user_id = (SELECT id FROM users WHERE email = $1 AND SUBSTRING(password, 1, 10) = $2);`
	rows, err := db.conn.Query(ctx, text, req.Email, req.Hash)
	if err != nil {
		return nil, err
	}
	defer rows.Close()
	var hives []string
	for rows.Next() {
		var hive httpType.Hive
		err := rows.Scan(&hive.NameHive)
		if err != nil {
			return nil, err
		}
		hives = append(hives, hive.NameHive)
	}
	return hives, nil
}
