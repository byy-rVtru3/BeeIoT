package postgres

import (
	"BeeIOT/internal/domain/models/httpType"
	"context"
)

const PARTPASSWD = 10

type id = int

func (db *Postgres) Registration(ctx context.Context, registration httpType.Registration) error {
	text := `INSERT INTO users (email, password) VALUES ($1, $2);`
	_, err := db.conn.Exec(ctx, text, registration.Email, registration.Password)
	if err != nil {
		return err
	}
	return nil
}

func (db *Postgres) IsExistUser(ctx context.Context, email string) (bool, error) {
	var idUser id
	text := `SELECT id FROM users WHERE email=$1;`
	err := db.conn.QueryRow(ctx, text, email).Scan(&idUser)
	if err != nil {
		if err.Error() == "no rows in result set" {
			return false, nil
		}
		return false, err
	}
	return true, nil
}

func (db *Postgres) Login(ctx context.Context, login httpType.Login) (string, error) {
	var pswd string
	text := `SELECT password FROM users WHERE email=$1;`
	err := db.conn.QueryRow(ctx, text, login.Email).Scan(&pswd)
	if err != nil {
		if err.Error() == "no rows in result set" {
			return pswd, nil
		}
		return pswd, err
	}
	return pswd, nil
}

func (db *Postgres) ChangePassword(ctx context.Context, user httpType.ChangePassword) error {
	text := `UPDATE users SET password=$1 WHERE email=$2;`
	_, err := db.conn.Exec(ctx, text, user.Password, user.Email)
	if err != nil {
		return err
	}
	return nil
}

func (db *Postgres) DeleteUser(ctx context.Context, email string) error {
	text := `DELETE FROM users WHERE email=$1;`
	_, err := db.conn.Exec(ctx, text, email)
	if err != nil {
		return err
	}
	return nil
}

func (db *Postgres) GetUserById(ctx context.Context, id int) (string, error) {
	var email string
	text := `SELECT email FROM users WHERE id=$1;`
	err := db.conn.QueryRow(ctx, text, id).Scan(&email)
	if err != nil {
		return "", err
	}
	return email, nil
}
