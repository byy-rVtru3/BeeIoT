package postgres

import (
	"BeeIOT/internal/domain/types/http"
	"context"
	"crypto/sha256"
)

const PARTPASSWD = 10

type id = int

func (db *Postgres) hashPassword(password string) string {
	hash := sha256.Sum256([]byte(password))
	return string(hash[:])
}

func (db *Postgres) Registration(ctx context.Context, registration http.Registration) error {
	text := `INSERT INTO users (email, password) VALUES ($1, $2);`
	_, err := db.conn.Exec(ctx, text, registration.Email, db.hashPassword(registration.Password))
	return err
}

// true if user is exist, false if not
func (db *Postgres) Login(ctx context.Context, login http.Login) (string, bool, error) {
	hashPasswd := db.hashPassword(login.Password)
	var bitOfPasswd string
	text := `SELECT password FROM users WHERE email=$1 AND password=$2;`
	err := db.conn.QueryRow(ctx, text, login.Email, hashPasswd).Scan(&bitOfPasswd)
	if err != nil {
		if err.Error() == "no rows in result set" {
			return "", false, nil
		}
		return "", false, err
	}
	return bitOfPasswd[:PARTPASSWD], true, nil
}

func (db *Postgres) ChangePassword(ctx context.Context, user http.ChangePassword) error {
	text := `UPDATE users SET password=$1 WHERE email=$2;`
	_, err := db.conn.Exec(ctx, text, db.hashPassword(user.Password), user.Email)
	return err
}

func (db *Postgres) DeleteUser(ctx context.Context, email string) error {
	text := `DELETE FROM users WHERE email=$1;`
	_, err := db.conn.Exec(ctx, text, email)
	return err
}
