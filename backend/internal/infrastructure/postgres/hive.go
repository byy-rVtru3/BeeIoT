package postgres

import (
	"BeeIOT/internal/domain/types/httpType"
	"context"

	"github.com/jackc/pgx/v5"
)

func (db *Postgres) NewHive(ctx context.Context, hive httpType.Hive) error {
	text := `INSERT INTO hives (user_id, name) 
			 VALUES ((SELECT id FROM users WHERE email = $1, $2);`
	_, err := db.conn.Exec(ctx, text, hive.Email, hive.NameHive)
	return err
}

func (db *Postgres) DeleteHive(ctx context.Context, hive httpType.Hive) error {
	text := `DELETE FROM hives 
			 WHERE user_id = (SELECT id FROM users WHERE email = $1) 
			 AND name = $2;`
	_, err := db.conn.Exec(ctx, text, hive.Email, hive.NameHive)
	return err
}

func (db *Postgres) GetHives(ctx context.Context, req *httpType.Hive) ([]httpType.Hive, error) { // хуйня с типами, должна быть вся дата ебучего улья
	var text string
	var rows pgx.Rows
	var err error
	if req == nil {
		text = `SELECT * FROM hives;`
		rows, err = db.conn.Query(ctx, text)
	} else {
		text = `SELECT * FROM hives WHERE user_id = (SELECT id FROM users WHERE email = $1);`
		rows, err = db.conn.Query(ctx, text, req.Email)
	}
	if err != nil {
		return nil, err
	}
	defer rows.Close()
	var hives []httpType.Hive
	for rows.Next() {
		var hive httpType.Hive
		err := rows.Scan(&hive.NameHive) // не сработает
		if err != nil {
			return nil, err
		}
		hives = append(hives, hive)
	}
	return hives, nil
}

func (db *Postgres) GetHiveByName(ctx context.Context, req httpType.Hive) (httpType.Hive, error) {
	text := `SELECT * FROM hives WHERE user_id = (SELECT id FROM users WHERE email = $1) AND name = $2;`
	row := db.conn.QueryRow(ctx, text, req.Email, req.NameHive)
	var hive httpType.Hive
	err := row.Scan(&hive.NameHive) // не сработает
	if err != nil {
		return httpType.Hive{}, err
	}
	return hive, nil
}

func (db *Postgres) UpdateHive(ctx context.Context, nameHive string, hive httpType.Hive) error {
	text := `UPDATE hives SET name = $1 
			 WHERE user_id = (SELECT id FROM users WHERE email = $2) AND name = $3;` // добавить остальные поля
	_, err := db.conn.Exec(ctx, text, hive.NameHive, hive.Email, nameHive)
	return err
}
