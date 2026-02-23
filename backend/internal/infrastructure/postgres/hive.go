package postgres

import (
	"BeeIOT/internal/domain/models/dbTypes"
	"context"

	"github.com/jackc/pgx/v5"
)

func (db *Postgres) NewHive(ctx context.Context, email, nameHive string) error {

	text := `INSERT INTO hives (user_id, name)
             SELECT id, $2 
             FROM users 
             WHERE email = $1`
	_, err := db.pull.Exec(ctx, text, email, nameHive)
	return err
}

func (db *Postgres) DeleteHive(ctx context.Context, email, nameHive string) error {
	text := `DELETE FROM hives h
			 USING users u
			 WHERE h.user_id = u.id
			   AND u.email = $1
		       AND h.name = $2;`
	_, err := db.pull.Exec(ctx, text, email, nameHive)
	return err
}

func (db *Postgres) GetHives(ctx context.Context, email string) ([]dbTypes.Hive, error) {
	var text string
	var rows pgx.Rows
	var err error
	if email == "" {
		text = `SELECT h.id, h.name, u.email, h.temperature_check, h.noise_check
		        FROM hives h
		        JOIN users u ON h.user_id = u.id;`
		rows, err = db.pull.Query(ctx, text)
	} else {
		text = `SELECT h.id, h.name, u.email, h.temperature_check, h.noise_check
                FROM hives h
                INNER JOIN users u ON h.user_id = u.id
                WHERE u.email = $1;`
		rows, err = db.pull.Query(ctx, text, email)
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
	text := `SELECT h.id, h.name, u.email, h.temperature_check, h.noise_check FROM hives h
        	 INNER JOIN users u ON h.user_id = u.id
             WHERE h.name = $2 AND u.email = $1;`
	row := db.pull.QueryRow(ctx, text, email, nameHive)
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
	_, err := db.pull.Exec(ctx, text, hive.NameHive, hive.Email, nameHive)
	return err
}

func (db *Postgres) GetEmailHiveBySensorID(ctx context.Context, sensorID string) (string, string, error) {
	text := `SELECT u.email, h.name FROM users u
			 JOIN hives h ON h.user_id = u.id
			 WHERE h.sensor_id = $1;`
	row := db.pull.QueryRow(ctx, text, sensorID)
	var email, hiveName string
	err := row.Scan(&email, &hiveName)
	if err != nil {
		return "", "", err
	}
	return email, hiveName, nil
}
