package postgres

import "context"

func (db *Postgres) SetSensor(ctx context.Context, email, sensor string) error {
	text := `INSERT INTO sensors SELECT u.id, $2 FROM users u WHERE u.email = $1`
	_, err := db.pull.Exec(ctx, text, email, sensor)
	return err
}

func (db *Postgres) GetSensors(ctx context.Context, email string) ([]string, error) {
	text := `SELECT s.sensor FROM sensors 
    JOIN users u ON s.user_id = u.id
    WHERE u.email = $1`
	rows, err := db.pull.Query(ctx, text, email)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var sensors []string
	for rows.Next() {
		var sensor string
		if err := rows.Scan(&sensor); err != nil {
			return nil, err
		}
		sensors = append(sensors, sensor)
	}
	return sensors, nil
}

func (db *Postgres) DeleteSensor(ctx context.Context, email, sensor string) error {
	text := `DELETE FROM sensors s USING users u 
	WHERE s.user_id = u.id AND u.email = $1 AND s.sensor = $2`
	_, err := db.pull.Exec(ctx, text, email, sensor)
	return err
}
