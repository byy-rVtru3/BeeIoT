package postgres

import (
	"BeeIOT/internal/domain/models/dbTypes"
	"BeeIOT/internal/domain/models/httpType"
	"context"

	"github.com/jackc/pgx/v5"
)

func (db *Postgres) CreateInstruction(ctx context.Context, req httpType.CreateInstructionRequest) (int, error) {
	var id int
	text := `INSERT INTO instructions (title, content) VALUES ($1, $2) RETURNING id;`
	err := db.pull.QueryRow(ctx, text, req.Title, req.Content).Scan(&id)
	return id, err
}

func (db *Postgres) GetInstructions(ctx context.Context) ([]dbTypes.Instruction, error) {
	text := `SELECT id, title, content, created_at FROM instructions ORDER BY created_at DESC;`
	rows, err := db.pull.Query(ctx, text)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var result []dbTypes.Instruction
	for rows.Next() {
		var item dbTypes.Instruction
		if err := rows.Scan(&item.ID, &item.Title, &item.Content, &item.CreatedAt); err != nil {
			return nil, err
		}
		result = append(result, item)
	}
	return result, nil
}

func (db *Postgres) DeleteInstruction(ctx context.Context, id int) error {
	text := `DELETE FROM instructions WHERE id=$1;`
	res, err := db.pull.Exec(ctx, text, id)
	if err != nil {
		return err
	}
	if res.RowsAffected() == 0 {
		return pgx.ErrNoRows
	}
	return nil
}
