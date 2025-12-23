package postgres

import (
	"BeeIOT/internal/domain/models/dbTypes"
	"context"

	"github.com/jackc/pgx/v5"
)

func (db *Postgres) NewHive(ctx context.Context, email, nameHive string) error {
	text := `INSERT INTO hives (user_id, name)
                         VALUES ((SELECT id FROM users WHERE email = $1), $2);`
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
		text = `SELECT id, name, (SELECT email FROM users WHERE id = user_id), temperature_check, noise_check FROM hives;`
		rows, err = db.conn.Query(ctx, text)
	} else {
		text = `SELECT id, name, (SELECT email FROM users WHERE id = user_id), temperature_check, noise_check FROM hives WHERE user_id = (SELECT id FROM users WHERE email = $1);`
		rows, err = db.conn.Query(ctx, text, email)
	}
	if err != nil {
		return nil, err
	}
	defer rows.Close()
	var hives []dbTypes.Hive
	for rows.Next() {
		var hive dbTypes.Hive
		err := rows.Scan(&hive.Id, &hive.NameHive, &hive.Email, &hive.DateTemperature, &hive.DateNoise)
		if err != nil {
			return nil, err
		}
		hives = append(hives, hive)
	}
	return hives, nil
}

func (db *Postgres) GetHiveByName(ctx context.Context, email, nameHive string) (dbTypes.Hive, error) {
	text := `SELECT id, name, (SELECT email FROM users WHERE id = user_id), temperature_check, noise_check FROM hives WHERE user_id = (SELECT id FROM users WHERE email = $1) AND name = $2;`
	row := db.conn.QueryRow(ctx, text, email, nameHive)
	var hive dbTypes.Hive
	err := row.Scan(&hive.Id, &hive.NameHive, &hive.Email, &hive.DateTemperature, &hive.DateNoise)
	if err != nil {
		return dbTypes.Hive{}, err
	}
	return hive, nil
}

func (db *Postgres) UpdateHive(ctx context.Context, nameHive string, hive dbTypes.Hive) error {
	text := `UPDATE hives SET name = $1 
                         WHERE user_id = (SELECT id FROM users WHERE email = $2) AND name = $3;`
	_, err := db.conn.Exec(ctx, text, hive.NameHive, hive.Email, nameHive)
	return err
}

func (db *Postgres) GetEmailHiveBySensorID(ctx context.Context, sensorID string) (string, string, error) {
	text := `SELECT u.email, h.name FROM users u
			 JOIN hives h ON h.user_id = u.id
			 WHERE h.sensor_id = $1;`
	row := db.conn.QueryRow(ctx, text, sensorID)
	var email, hiveName string
	err := row.Scan(&email, &hiveName)
	if err != nil {
		return "", "", err
	}
	return email, hiveName, nil
}
