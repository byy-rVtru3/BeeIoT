package postgres

import (
	"BeeIOT/internal/domain/models/httpType"
	"context"
	"time"
)

func (db *Postgres) NewTask(ctx context.Context, task httpType.Task) error {
	text := `INSERT INTO tasks (user_id, hive_id, tasks_id, time) 
			 VALUES (
				(SELECT id FROM users WHERE email = $1 AND SUBSTRING(password, 1, 10) = $2),
				(SELECT id FROM hives WHERE name = $3 AND user_id = (SELECT id FROM users WHERE email = $1 AND SUBSTRING(password, 1, 10) = $2)),
				(SELECT task_id FROM name_tasks WHERE name = $4),
				$5
			 );`
	_, err := db.conn.Exec(ctx, text, task.Email, task.Hash, task.Hive, task.Name, task.Time)
	return err
}

func (db *Postgres) DeleteTask(ctx context.Context, task httpType.Task) error {
	text := `DELETE FROM tasks
			 WHERE task_id = (
				 SELECT t.task_id FROM tasks t
				 JOIN hive h ON t.hive_id = h.hive_id
				 JOIN users u ON h.user_id = u.user_id
				 JOIN name_tasks nt ON t.name_tasks_id = nt.task_id
				 WHERE u.email = $1 AND SUBSTRING(u.password, 1, 10) = $2 
				 AND h.name = $3 AND nt.name = $4
			 );`
	_, err := db.conn.Exec(ctx, text, task.Email, task.Hash, task.Hive, task.Name)
	return err
}

func (db *Postgres) GetTasksByUserID(ctx context.Context, task httpType.Task) ([]httpType.Task, error) {
	text := `SELECT t.task_id, nt.name, t.time, u.email, h.name
			 FROM tasks t
			 JOIN hive h ON t.hive_id = h.hive_id
			 JOIN name_tasks nt ON t.name_tasks_id = nt.task_id
			 JOIN users u ON h.user_id = u.user_id
			 WHERE u.email = $1 AND SUBSTRING(u.password, 1, 10) = $2;`
	rows, err := db.conn.Query(ctx, text, task.Email, task.Hash)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var tasks []httpType.Task
	for rows.Next() {
		var taskResult httpType.Task
		var taskID int
		var taskTime int64
		err := rows.Scan(&taskID, &taskResult.Name, &taskTime, &taskResult.Email, &taskResult.Hive)
		if err != nil {
			return nil, err
		}

		taskResult.Time = time.Unix(0, taskTime*1000)
		taskResult.Hash = task.Hash
		tasks = append(tasks, taskResult)
	}
	return tasks, nil
}

func (db *Postgres) GetTasksByHiveID(ctx context.Context, task httpType.Task) ([]httpType.Task, error) {
	text := `SELECT t.task_id, nt.name, t.time, u.email, h.name
			 FROM tasks t
			 JOIN hive h ON t.hive_id = h.hive_id
			 JOIN name_tasks nt ON t.name_tasks_id = nt.task_id
			 JOIN users u ON h.user_id = u.user_id
			 WHERE h.name = $1 AND u.email = $2 AND SUBSTRING(u.password, 1, 10) = $3;`
	rows, err := db.conn.Query(ctx, text, task.Hive, task.Email, task.Hash)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var tasks []httpType.Task
	for rows.Next() {
		var taskResult httpType.Task
		var taskID int
		var taskTime int64
		err := rows.Scan(&taskID, &taskResult.Name, &taskTime, &taskResult.Email, &taskResult.Hive)
		if err != nil {
			return nil, err
		}

		taskResult.Time = time.Unix(0, taskTime*1000)
		taskResult.Hash = task.Hash
		tasks = append(tasks, taskResult)
	}
	return tasks, nil
}

func (db *Postgres) getTasksSinceTime(ctx context.Context, task httpType.Task, sinceTime time.Time) ([]httpType.Task, error) {
	text := `SELECT t.task_id, nt.name, t.time, u.email, h.name
			 FROM tasks t
			 JOIN hive h ON t.hive_id = h.hive_id
			 JOIN name_tasks nt ON t.name_tasks_id = nt.task_id
			 JOIN users u ON h.user_id = u.user_id
			 WHERE h.name = $1 AND u.email = $2 AND SUBSTRING(u.password, 1, 10) = $3 AND t.time >= $4;`

	sinceTimeMicros := sinceTime.UnixNano() / 1000

	rows, err := db.conn.Query(ctx, text, task.Hive, task.Email, task.Hash, sinceTimeMicros)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var tasks []httpType.Task
	for rows.Next() {
		var taskResult httpType.Task
		var taskID int
		var taskTime int64
		err := rows.Scan(&taskID, &taskResult.Name, &taskTime, &taskResult.Email, &taskResult.Hive)
		if err != nil {
			return nil, err
		}

		taskResult.Time = time.Unix(0, taskTime*1000)
		taskResult.Hash = task.Hash
		tasks = append(tasks, taskResult)
	}
	return tasks, nil
}

func (db *Postgres) GetTaskForDay(ctx context.Context, task httpType.Task) ([]httpType.Task, error) {
	dayAgo := time.Now().Add(-24 * time.Hour)
	return db.getTasksSinceTime(ctx, task, dayAgo)
}

func (db *Postgres) GetTaskForWeek(ctx context.Context, task httpType.Task) ([]httpType.Task, error) {
	weekAgo := time.Now().Add(-7 * 24 * time.Hour)
	return db.getTasksSinceTime(ctx, task, weekAgo)
}

func (db *Postgres) GetTaskForMonth(ctx context.Context, task httpType.Task) ([]httpType.Task, error) {
	monthAgo := time.Now().Add(-30 * 24 * time.Hour)
	return db.getTasksSinceTime(ctx, task, monthAgo)
}
