package postgres

import (
	"BeeIOT/internal/domain/models/dbTypes"
	"BeeIOT/internal/domain/models/httpType"
	"context"
	"fmt"
)

func (d *Postgres) NewQueen(ctx context.Context, email, name, startDate string) error {
	q := `INSERT INTO queens (email, name, start_date) VALUES ($1, $2, $3::date)`
	_, err := d.pull.Exec(ctx, q, email, name, startDate)
	if err != nil {
		return fmt.Errorf("failed to insert new queen: %w", err)
	}
	return nil
}

func (d *Postgres) GetQueens(ctx context.Context, email string) ([]dbTypes.Queen, error) {
	q := `SELECT id, email, name, start_date FROM queens WHERE email = $1`
	rows, err := d.pull.Query(ctx, q, email)
	if err != nil {
		return nil, fmt.Errorf("failed to get queens: %w", err)
	}
	defer rows.Close()

	var queens []dbTypes.Queen
	for rows.Next() {
		var qn dbTypes.Queen
		if err := rows.Scan(&qn.Id, &qn.Email, &qn.Name, &qn.StartDate); err != nil {
			return nil, fmt.Errorf("failed to scan queen: %w", err)
		}
		queens = append(queens, qn)
	}
	return queens, nil
}

func (d *Postgres) GetQueenByName(ctx context.Context, email, name string) (dbTypes.Queen, error) {
	q := `SELECT id, email, name, start_date FROM queens WHERE email = $1 AND name = $2`
	var qn dbTypes.Queen
	err := d.pull.QueryRow(ctx, q, email, name).Scan(&qn.Id, &qn.Email, &qn.Name, &qn.StartDate)
	if err != nil {
		return qn, fmt.Errorf("failed to get queen by name: %w", err)
	}
	return qn, nil
}

func (d *Postgres) DeleteQueen(ctx context.Context, email, name string) error {
	q := `DELETE FROM queens WHERE email = $1 AND name = $2`
	res, err := d.pull.Exec(ctx, q, email, name)
	if err != nil {
		return fmt.Errorf("failed to delete queen: %w", err)
	}
	if res.RowsAffected() == 0 {
		return fmt.Errorf("queen not found or already deleted")
	}
	return nil
}

func (d *Postgres) UpdateQueen(ctx context.Context, email string, data httpType.UpdateQueen) error {
	args := []interface{}{email, data.OldName}
	queryStr := "UPDATE queens SET "
	argId := 3

	if data.NewName != nil {
		queryStr += fmt.Sprintf("name = $%d", argId)
		args = append(args, *data.NewName)
		argId++
	}
	if data.StartDate != nil {
		if argId > 3 {
			queryStr += ", "
		}
		queryStr += fmt.Sprintf("start_date = $%d::date", argId)
		args = append(args, *data.StartDate)
		argId++
	}

	if argId == 3 {
		return nil // Nothing to update
	}

	queryStr += " WHERE email = $1 AND name = $2"

	res, err := d.pull.Exec(ctx, queryStr, args...)
	if err != nil {
		return fmt.Errorf("failed to update queen: %w", err)
	}
	if res.RowsAffected() == 0 {
		return fmt.Errorf("queen not found")
	}
	return nil
}
