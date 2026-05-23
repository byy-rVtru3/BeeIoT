package postgres

import (
	"BeeIOT/internal/domain/models/dbTypes"
	"BeeIOT/internal/domain/models/httpType"
	"context"
	"fmt"
	"time"

	"github.com/google/uuid"
)

func (db *Postgres) CreateTask(ctx context.Context, email string, req httpType.CreateTaskRequest) (string, error) {
	taskID := uuid.New().String()
	q := `INSERT INTO tasks (id, email, hive_name, title, description, created_at)
	      VALUES ($1, $2, $3, $4, $5, $6)`
	_, err := db.pull.Exec(ctx, q, taskID, email, req.HiveName, req.Title, req.Description, time.Now())
	if err != nil {
		return "", fmt.Errorf("failed to create task: %w", err)
	}
	return taskID, nil
}

func (db *Postgres) GetTasks(ctx context.Context, email, hiveName string) ([]dbTypes.Task, error) {
	q := `SELECT id, email, hive_name, title, description, created_at FROM tasks
	      WHERE email = $1`
	args := []interface{}{email}

	if hiveName != "" {
		q += ` AND hive_name = $2`
		args = append(args, hiveName)
	}
	q += ` ORDER BY created_at DESC`

	rows, err := db.pull.Query(ctx, q, args...)
	if err != nil {
		return nil, fmt.Errorf("failed to get tasks: %w", err)
	}
	defer rows.Close()

	var tasks []dbTypes.Task
	for rows.Next() {
		var task dbTypes.Task
		err := rows.Scan(&task.ID, &task.Email, &task.HiveName, &task.Title, &task.Description, &task.CreatedAt)
		if err != nil {
			return nil, fmt.Errorf("failed to scan task: %w", err)
		}
		tasks = append(tasks, task)
	}
	return tasks, nil
}

func (db *Postgres) GetTaskByID(ctx context.Context, taskID string) (dbTypes.Task, error) {
	q := `SELECT id, email, hive_name, title, description, created_at FROM tasks WHERE id = $1`
	var task dbTypes.Task
	err := db.pull.QueryRow(ctx, q, taskID).Scan(&task.ID, &task.Email, &task.HiveName, &task.Title, &task.Description, &task.CreatedAt)
	if err != nil {
		return task, fmt.Errorf("task not found: %w", err)
	}
	return task, nil
}

func (db *Postgres) UpdateTask(ctx context.Context, email string, req httpType.UpdateTaskRequest) error {
	task, err := db.GetTaskByID(ctx, req.ID)
	if err != nil {
		return err
	}

	if task.Email != email {
		return fmt.Errorf("unauthorized to update this task")
	}

	if req.Title != nil {
		task.Title = *req.Title
	}
	if req.Description != nil {
		task.Description = *req.Description
	}

	q := `UPDATE tasks SET title = $1, description = $2 WHERE id = $3`
	res, err := db.pull.Exec(ctx, q, task.Title, task.Description, req.ID)
	if err != nil {
		return fmt.Errorf("failed to update task: %w", err)
	}
	if res.RowsAffected() == 0 {
		return fmt.Errorf("task not found")
	}
	return nil
}

func (db *Postgres) DeleteTask(ctx context.Context, email, taskID string) error {
	task, err := db.GetTaskByID(ctx, taskID)
	if err != nil {
		return err
	}

	if task.Email != email {
		return fmt.Errorf("unauthorized to delete this task")
	}

	q := `DELETE FROM tasks WHERE id = $1`
	res, err := db.pull.Exec(ctx, q, taskID)
	if err != nil {
		return fmt.Errorf("failed to delete task: %w", err)
	}
	if res.RowsAffected() == 0 {
		return fmt.Errorf("task not found")
	}
	return nil
}
