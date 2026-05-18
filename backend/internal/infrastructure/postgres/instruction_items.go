package postgres

import (
	"BeeIOT/internal/domain/models/dbTypes"
	"BeeIOT/internal/domain/models/httpType"
	"context"
	"fmt"

	"github.com/jackc/pgx/v5"
)

func (db *Postgres) GetInstructionItems(ctx context.Context) ([]dbTypes.InstructionItem, error) {
	text := `SELECT id, title, body, numbered, position, created_at, updated_at
	         FROM instruction_items
	         ORDER BY position;`
	rows, err := db.pull.Query(ctx, text)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var result []dbTypes.InstructionItem
	for rows.Next() {
		var it dbTypes.InstructionItem
		if err := rows.Scan(&it.ID, &it.Title, &it.Body, &it.Numbered, &it.Position, &it.CreatedAt, &it.UpdatedAt); err != nil {
			return nil, err
		}
		result = append(result, it)
	}
	return result, rows.Err()
}

func (db *Postgres) CreateInstructionItem(ctx context.Context, req httpType.CreateInstructionItemRequest) (dbTypes.InstructionItem, error) {
	var it dbTypes.InstructionItem
	tx, err := db.pull.Begin(ctx)
	if err != nil {
		return it, err
	}
	defer func() { _ = tx.Rollback(ctx) }()

	var pos int
	if req.Position != nil {
		pos = *req.Position
	} else {
		err = tx.QueryRow(ctx, `SELECT COALESCE(MAX(position), 0) + 1 FROM instruction_items;`).Scan(&pos)
		if err != nil {
			return it, err
		}
	}

	text := `INSERT INTO instruction_items (title, body, numbered, position)
	         VALUES ($1, $2, $3, $4)
	         RETURNING id, title, body, numbered, position, created_at, updated_at;`
	err = tx.QueryRow(ctx, text, req.Title, req.Body, req.Numbered, pos).
		Scan(&it.ID, &it.Title, &it.Body, &it.Numbered, &it.Position, &it.CreatedAt, &it.UpdatedAt)
	if err != nil {
		return it, err
	}

	if err := tx.Commit(ctx); err != nil {
		return it, err
	}
	return it, nil
}

func (db *Postgres) UpdateInstructionItem(ctx context.Context, id string, req httpType.UpdateInstructionItemRequest) (dbTypes.InstructionItem, error) {
	var it dbTypes.InstructionItem
	text := `UPDATE instruction_items
	         SET title    = COALESCE($2, title),
	             body     = COALESCE($3, body),
	             numbered = COALESCE($4, numbered),
	             updated_at = now()
	         WHERE id = $1
	         RETURNING id, title, body, numbered, position, created_at, updated_at;`
	err := db.pull.QueryRow(ctx, text, id, req.Title, req.Body, req.Numbered).
		Scan(&it.ID, &it.Title, &it.Body, &it.Numbered, &it.Position, &it.CreatedAt, &it.UpdatedAt)
	return it, err
}

func (db *Postgres) DeleteInstructionItem(ctx context.Context, id string) error {
	res, err := db.pull.Exec(ctx, `DELETE FROM instruction_items WHERE id = $1;`, id)
	if err != nil {
		return err
	}
	if res.RowsAffected() == 0 {
		return pgx.ErrNoRows
	}
	return nil
}

func (db *Postgres) ReorderInstructionItems(ctx context.Context, order []string) ([]dbTypes.InstructionItem, error) {
	tx, err := db.pull.Begin(ctx)
	if err != nil {
		return nil, err
	}
	defer func() { _ = tx.Rollback(ctx) }()

	var existing int
	if err := tx.QueryRow(ctx, `SELECT COUNT(*) FROM instruction_items;`).Scan(&existing); err != nil {
		return nil, err
	}
	if existing != len(order) {
		return nil, fmt.Errorf("order length mismatch: got %d, expected %d", len(order), existing)
	}

	if _, err := tx.Exec(ctx, `UPDATE instruction_items SET position = -position;`); err != nil {
		return nil, err
	}

	for i, id := range order {
		tag, err := tx.Exec(ctx,
			`UPDATE instruction_items SET position = $2, updated_at = now() WHERE id = $1;`,
			id, i+1)
		if err != nil {
			return nil, err
		}
		if tag.RowsAffected() == 0 {
			return nil, fmt.Errorf("id not found: %s", id)
		}
	}

	if err := tx.Commit(ctx); err != nil {
		return nil, err
	}
	return db.GetInstructionItems(ctx)
}
