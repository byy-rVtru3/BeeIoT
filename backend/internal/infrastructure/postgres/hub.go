package postgres

import (
	"BeeIOT/internal/domain/models/dbTypes"
	"BeeIOT/internal/domain/models/httpType"
	"context"
	"fmt"
)

func (d *Postgres) NewHub(ctx context.Context, email, nameHub, sensorName string) error {
	q := `INSERT INTO hubs (email, name, sensor) VALUES ($1, $2, $3)`
	_, err := d.pull.Exec(ctx, q, email, nameHub, sensorName)
	if err != nil {
		return fmt.Errorf("failed to insert new hub: %w", err)
	}
	return nil
}

func (d *Postgres) GetHubs(ctx context.Context, email string) ([]dbTypes.Hub, error) {
	q := `SELECT id, name, email, sensor FROM hubs WHERE email = $1`
	rows, err := d.pull.Query(ctx, q, email)
	if err != nil {
		return nil, fmt.Errorf("failed to get hubs: %w", err)
	}
	defer rows.Close()

	var hubs []dbTypes.Hub
	for rows.Next() {
		var hub dbTypes.Hub
		if err := rows.Scan(&hub.Id, &hub.NameHub, &hub.Email, &hub.Sensor); err != nil {
			return nil, fmt.Errorf("failed to scan hub: %w", err)
		}
		hubs = append(hubs, hub)
	}
	return hubs, nil
}

func (d *Postgres) GetHubBySensor(ctx context.Context, email, sensor string) (dbTypes.Hub, error) {
	q := `SELECT id, name, email, sensor FROM hubs WHERE email = $1 AND sensor = $2`
	var hub dbTypes.Hub
	err := d.pull.QueryRow(ctx, q, email, sensor).Scan(&hub.Id, &hub.NameHub, &hub.Email, &hub.Sensor)
	if err != nil {
		return hub, fmt.Errorf("failed to get hub by sensor: %w", err)
	}
	return hub, nil
}

func (d *Postgres) DeleteHub(ctx context.Context, email, nameHub string) error {
	q := `DELETE FROM hubs WHERE email = $1 AND name = $2`
	res, err := d.pull.Exec(ctx, q, email, nameHub)
	if err != nil {
		return fmt.Errorf("failed to delete hub: %w", err)
	}
	if res.RowsAffected() == 0 {
		return fmt.Errorf("hub not found or already deleted")
	}
	return nil
}

func (d *Postgres) UpdateHub(ctx context.Context, email string, data httpType.UpdateHub) error {
	if data.Name == nil {
		return nil // Nothing to update
	}

	q := `UPDATE hubs SET name = $3 WHERE email = $1 AND sensor = $2`
	res, err := d.pull.Exec(ctx, q, email, data.ID, *data.Name)
	if err != nil {
		return fmt.Errorf("failed to update hub: %w", err)
	}
	if res.RowsAffected() == 0 {
		return fmt.Errorf("hub not found")
	}
	return nil
}
