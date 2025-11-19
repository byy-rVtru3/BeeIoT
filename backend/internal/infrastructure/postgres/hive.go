package postgres

import (
	"BeeIOT/internal/domain/models/dbTypes"
	"context"

	"github.com/jackc/pgx/v5"
)

func (db *Postgres) NewHive(ctx context.Context, email, nameHive string) error {
	text := `INSERT INTO hives (user_id, name)
			 VALUES ((SELECT id FROM users WHERE email = $1, $2);`
	_, err := db.conn.Exec(ctx, text, email, nameHive)
	return err
}

func (db *Postgres) DeleteHive(ctx context.Context, email, nameHive string) error {
	text := `DELETE FROM hives 
			 WHERE user_id = (SELECT id FROM users WHERE email = $1) 
			 AND name = $2;`
	_, err := db.conn.Exec(ctx, text, email, nameHive)
	return err
}

func (db *Postgres) GetHives(ctx context.Context, email string) ([]dbTypes.Hive, error) {
	var text string
	var rows pgx.Rows
	var err error
	if email == "" {
		text = `SELECT * FROM hives;`
		rows, err = db.conn.Query(ctx, text)
	} else {
		text = `SELECT * FROM hives WHERE user_id = (SELECT id FROM users WHERE email = $1);`
		rows, err = db.conn.Query(ctx, text, email)
	}
	if err != nil {
		return nil, err
	}
	defer rows.Close()
	var hives []dbTypes.Hive
	for rows.Next() {
		var hive dbTypes.Hive
		err := rows.Scan(&hive.NameHive) // не сработает
		if err != nil {
			return nil, err
		}
		hives = append(hives, hive)
	}
	return hives, nil
}

func (db *Postgres) GetHiveByName(ctx context.Context, email, nameHive string) (dbTypes.Hive, error) {
	text := `SELECT * FROM hives WHERE user_id = (SELECT id FROM users WHERE email = $1) AND name = $2;`
	row := db.conn.QueryRow(ctx, text, email, nameHive)
	var hive dbTypes.Hive
	err := row.Scan(&hive.NameHive) // не сработает, нужно на отедльные параметры разбить
	if err != nil {
		return dbTypes.Hive{}, err
	}
	return hive, nil
}

func (db *Postgres) UpdateHive(ctx context.Context, nameHive string, hive dbTypes.Hive) error {
	text := `UPDATE hives SET name = $1 
			 WHERE user_id = (SELECT id FROM users WHERE email = $2) AND name = $3;` // добавить остальные поля
	_, err := db.conn.Exec(ctx, text, hive.NameHive, hive.Email, nameHive)
	return err
}
