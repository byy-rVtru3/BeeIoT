package postgres

import (
	"BeeIOT/internal/domain/models/dbTypes"
	"BeeIOT/internal/domain/models/httpType"
	"context"
	"time"

	"github.com/jackc/pgx/v5"
)

func (db *Postgres) NewHive(ctx context.Context, email, nameHive, sensorName string) error {

	text := `INSERT INTO hives (user_id, name, sensor_id)
             SELECT u.id, $2, s.id
             FROM users u
             LEFT JOIN sensors s ON s.sensor_id = $3 AND s.user_id = u.id
             WHERE u.email = $1`
	_, err := db.pull.Exec(ctx, text, email, nameHive, sensorName)
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

func (db *Postgres) GetHives(ctx context.Context, email string, active *bool) ([]dbTypes.Hive, error) {
	base := `SELECT h.id, h.name, u.email, h.temperature_check, h.noise_check, COALESCE(s.sensor_id, ''), h.status, COALESCE(hu.sensor, ''), COALESCE(q.name, '')
	        FROM hives h
	        JOIN users u ON h.user_id = u.id
	        LEFT JOIN sensors s ON h.sensor_id = s.id
	        LEFT JOIN hubs hu ON h.hub_id = hu.id
	        LEFT JOIN queens q ON h.queen_id = q.id`

	var rows pgx.Rows
	var err error

	switch {
	case email == "" && active == nil:
		rows, err = db.pull.Query(ctx, base)
	case email == "" && active != nil:
		rows, err = db.pull.Query(ctx, base+" WHERE h.status = $1", *active)
	case email != "" && active == nil:
		rows, err = db.pull.Query(ctx, base+" WHERE u.email = $1", email)
	default:
		rows, err = db.pull.Query(ctx, base+" WHERE u.email = $1 AND h.status = $2", email, *active)
	}
	if err != nil {
		return nil, err
	}
	defer rows.Close()
	var hives []dbTypes.Hive
	for rows.Next() {
		var hive dbTypes.Hive
		err := rows.Scan(&hive.Id, &hive.NameHive, &hive.Email, &hive.DateTemperature, &hive.DateNoise, &hive.SensorID, &hive.Status, &hive.HubName, &hive.QueenName)
		if err != nil {
			return nil, err
		}
		hives = append(hives, hive)
	}
	return hives, nil
}

func (db *Postgres) GetHiveByName(ctx context.Context, email, nameHive string, active *bool) (dbTypes.Hive, error) {
	base := `SELECT h.id, h.name, u.email, h.temperature_check, h.noise_check, COALESCE(s.sensor_id, ''), h.status, COALESCE(hu.sensor, ''), COALESCE(q.name, '')
	        FROM hives h
	        INNER JOIN users u ON h.user_id = u.id
	        LEFT JOIN sensors s ON h.sensor_id = s.id
	        LEFT JOIN hubs hu ON h.hub_id = hu.id
	        LEFT JOIN queens q ON h.queen_id = q.id
	        WHERE h.name = $2 AND u.email = $1`

	var row pgx.Row
	if active != nil {
		row = db.pull.QueryRow(ctx, base+" AND h.status = $3", email, nameHive, *active)
	} else {
		row = db.pull.QueryRow(ctx, base, email, nameHive)
	}
	var hive dbTypes.Hive
	err := row.Scan(&hive.Id, &hive.NameHive, &hive.Email, &hive.DateTemperature, &hive.DateNoise, &hive.SensorID, &hive.Status, &hive.HubName, &hive.QueenName)
	if err != nil {
		return dbTypes.Hive{}, err
	}
	return hive, nil
}

// проверить
func (db *Postgres) UpdateHive(ctx context.Context, email string, data httpType.UpdateHive) error {
	tx, err := db.pull.Begin(ctx)
	if err != nil {
		return err
	}
	defer func() {
		_ = tx.Rollback(ctx)
	}()

	var hiveID int
	err = tx.QueryRow(ctx, `SELECT h.id FROM hives h JOIN users u ON h.user_id = u.id WHERE u.email = $1 AND h.name = $2`, email, data.OldName).Scan(&hiveID)
	if err != nil {
		return err
	}

	if data.NewName != nil && *data.NewName != "" {
		_, err = tx.Exec(ctx, `UPDATE hives SET name = $1 WHERE id = $2`, *data.NewName, hiveID)
		if err != nil {
			return err
		}
	}

	if data.Active != nil {
		_, err = tx.Exec(ctx, `UPDATE hives SET status = $1 WHERE id = $2`, *data.Active, hiveID)
		if err != nil {
			return err
		}
	}

	if data.Sensor != nil && *data.Sensor != "" {
		var sensorID int
		err = tx.QueryRow(ctx, `SELECT s.id FROM sensors s JOIN users u ON s.user_id = u.id WHERE u.email = $1 AND s.sensor_id = $2`, email, *data.Sensor).Scan(&sensorID)
		if err != nil {
			return err
		}
		_, err = tx.Exec(ctx, `UPDATE hives SET sensor_id = $1 WHERE id = $2`, sensorID, hiveID)
		if err != nil {
			return err
		}
	} else if data.Sensor != nil && *data.Sensor == "" {
		_, err = tx.Exec(ctx, `UPDATE hives SET sensor_id = NULL WHERE id = $1`, hiveID)
		if err != nil {
			return err
		}
	}

	return tx.Commit(ctx)
}

func (db *Postgres) UpdateHiveTemperatureCheck(ctx context.Context, hiveId int, t time.Time) error {
	text := `UPDATE hives SET temperature_check = $1 WHERE id = $2;`
	_, err := db.pull.Exec(ctx, text, t, hiveId)
	return err
}

func (db *Postgres) UpdateHiveNoiseCheck(ctx context.Context, hiveId int, t time.Time) error {
	text := `UPDATE hives SET noise_check = $1 WHERE id = $2;`
	_, err := db.pull.Exec(ctx, text, t, hiveId)
	return err
}

func (db *Postgres) UpdateHiveStatus(ctx context.Context, email, name string, status bool) error {
	text := `UPDATE hives SET status = $3
	         WHERE name = $2 AND user_id = (SELECT id FROM users WHERE email = $1)`
	_, err := db.pull.Exec(ctx, text, email, name, status)
	return err
}

func (db *Postgres) GetEmailHiveBySensorID(ctx context.Context, sensorID string) (string, string, error) {
	text := `SELECT u.email, h.name FROM users u
			 JOIN hives h ON h.user_id = u.id
			 JOIN sensors s ON h.sensor_id = s.id
			 WHERE s.sensor_id = $1;`
	row := db.pull.QueryRow(ctx, text, sensorID)
	var email, hiveName string
	err := row.Scan(&email, &hiveName)
	if err != nil {
		return "", "", err
	}
	return email, hiveName, nil
}

func (db *Postgres) SetSensorToHive(ctx context.Context, email, nameHive, sensor string) error {
	tr, err := db.pull.Begin(ctx)
	if err != nil {
		return err
	}
	defer func() {
		_ = tr.Rollback(ctx)
	}()
	text := `UPDATE hives SET sensor_id = s.id 
FROM sensors s JOIN users u ON s.user_id = u.id
WHERE s.sensor = $1 AND u.email = $2 AND hives.name = $3`
	_, err = tr.Exec(ctx, text, email, nameHive, sensor)
	if err != nil {
		return err
	}
	text = `UPDATE sensors SET active = true 
FROM users u JOIN hives h ON h.sensor_id = sensors.id
WHERE sensors.sensor = $1 AND u.email = $2 AND h.name = $3`
	_, err = tr.Exec(ctx, text, email, nameHive, sensor)
	if err != nil {
		return err
	}
	return tr.Commit(ctx)
}

func (db *Postgres) RemoveSensorFromHive(ctx context.Context, email, nameHive string) error {
	tr, err := db.pull.Begin(ctx)
	if err != nil {
		return err
	}
	defer func() {
		_ = tr.Rollback(ctx)
	}()
	text := `UPDATE hives SET sensor_id = NULL 
WHERE name = $1 AND user_id = (SELECT id FROM users WHERE email = $2)`
	_, err = tr.Exec(ctx, text, nameHive, email)
	if err != nil {
		return err
	}
	text = `UPDATE sensors SET active = false 
FROM users u JOIN hives h ON h.sensor_id = sensors.id
WHERE u.email = $1 AND h.name = $2`
	_, err = tr.Exec(ctx, text, email, nameHive)
	if err != nil {
		return err
	}
	return tr.Commit(ctx)
}

func (db *Postgres) LinkHubToHive(ctx context.Context, email, hiveName, hubName string) error {
	var err error
	if hubName == "" {
		q := `UPDATE hives SET hub_id = NULL WHERE user_id = (SELECT id FROM users WHERE email = $1) AND name = $2`
		_, err = db.pull.Exec(ctx, q, email, hiveName)
	} else {
		q := `UPDATE hives SET hub_id = (SELECT id FROM hubs WHERE email = $1 AND sensor = $3) WHERE user_id = (SELECT id FROM users WHERE email = $1) AND name = $2`
		_, err = db.pull.Exec(ctx, q, email, hiveName, hubName)
	}
	if err != nil {
		return err
	}
	return nil
}

func (db *Postgres) LinkQueenToHive(ctx context.Context, email, hiveName, queenName string) error {
	var err error
	if queenName == "" {
		q := `UPDATE hives SET queen_id = NULL WHERE user_id = (SELECT id FROM users WHERE email = $1) AND name = $2`
		_, err = db.pull.Exec(ctx, q, email, hiveName)
	} else {
		q := `UPDATE hives SET queen_id = (SELECT id FROM queens WHERE email = $1 AND name = $3) WHERE user_id = (SELECT id FROM users WHERE email = $1) AND name = $2`
		_, err = db.pull.Exec(ctx, q, email, hiveName, queenName)
	}
	if err != nil {
		return err
	}
	return nil
}
