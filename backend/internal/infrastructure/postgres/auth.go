package postgres

import (
	"BeeIOT/internal/domain/models/httpType"
	"context"
	"errors"

	"github.com/jackc/pgx/v5"
)

type id = int

func (db *Postgres) Registration(ctx context.Context, registration httpType.Registration) error {
	text := `INSERT INTO users (email, name, password) VALUES ($1, $2, $3);`
	_, err := db.pull.Exec(ctx, text, registration.Email, registration.Name, registration.Password)
	return err
}

func (db *Postgres) IsExistUser(ctx context.Context, email string) (bool, error) {
	var idUser id
	text := `SELECT id FROM users WHERE email=$1;`
	err := db.pull.QueryRow(ctx, text, email).Scan(&idUser)
	if err != nil {
		if errors.Is(err, pgx.ErrNoRows) {
			return false, nil
		}
		return false, err
	}
	return true, nil
}

func (db *Postgres) Login(ctx context.Context, login httpType.Login) (string, error) {
	var pswd string
	text := `SELECT password FROM users WHERE email=$1;`
	err := db.pull.QueryRow(ctx, text, login.Email).Scan(&pswd)
	if err != nil {
		if errors.Is(err, pgx.ErrNoRows) {
			return pswd, nil
		}
		return pswd, err
	}
	return pswd, nil
}

func (db *Postgres) ChangePassword(ctx context.Context, user httpType.ChangePassword) error {
	text := `UPDATE users SET password=$1 WHERE email=$2;`
	_, err := db.pull.Exec(ctx, text, user.Password, user.Email)
	return err
}

func (db *Postgres) DeleteUser(ctx context.Context, email string) error {
	text := `DELETE FROM users WHERE email=$1;`
	_, err := db.pull.Exec(ctx, text, email)
	return err
}

func (db *Postgres) GetUserById(ctx context.Context, id int) (string, error) {
	var email string
	text := `SELECT email FROM users WHERE id=$1;`
	err := db.pull.QueryRow(ctx, text, id).Scan(&email)
	return email, err
}

func (db *Postgres) ChangeNameUser(ctx context.Context, email string, name string) error {
	text := `UPDATE users SET name=$1 WHERE email=$2;`
	_, err := db.pull.Exec(ctx, text, name, email)
	return err
}
