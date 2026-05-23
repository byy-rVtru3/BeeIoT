package postgres

import (
	"BeeIOT/internal/domain/models/dbTypes"
	"BeeIOT/internal/domain/models/httpType"
	"context"
)

func (db *Postgres) GetAppDescription(ctx context.Context) (dbTypes.AppDescription, error) {
	var d dbTypes.AppDescription
	var updatedBy *string
	text := `SELECT title, short, "full", updated_at, updated_by FROM app_description WHERE id = 1;`
	err := db.pull.QueryRow(ctx, text).Scan(&d.Title, &d.Short, &d.Full, &d.UpdatedAt, &updatedBy)
	if err != nil {
		return d, err
	}
	if updatedBy != nil {
		d.UpdatedBy = *updatedBy
	}
	return d, nil
}

func (db *Postgres) UpsertAppDescription(ctx context.Context, req httpType.UpdateAppDescriptionRequest, updatedBy string) (dbTypes.AppDescription, error) {
	text := `INSERT INTO app_description (id, title, short, "full", updated_at, updated_by)
	         VALUES (1, $1, $2, $3, now(), $4)
	         ON CONFLICT (id) DO UPDATE
	         SET title = EXCLUDED.title,
	             short = EXCLUDED.short,
	             "full" = EXCLUDED."full",
	             updated_at = now(),
	             updated_by = EXCLUDED.updated_by
	         RETURNING title, short, "full", updated_at, updated_by;`
	var d dbTypes.AppDescription
	var ub *string
	err := db.pull.QueryRow(ctx, text, req.Title, req.Short, req.Full, updatedBy).
		Scan(&d.Title, &d.Short, &d.Full, &d.UpdatedAt, &ub)
	if err != nil {
		return d, err
	}
	if ub != nil {
		d.UpdatedBy = *ub
	}
	return d, nil
}
