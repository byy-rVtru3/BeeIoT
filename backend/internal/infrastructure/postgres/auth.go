package postgres

import (
	"BeeIOT/internal/domain/types/httpType"
	"context"
	"log/slog"
)

const PARTPASSWD = 10

type id = int

func (db *Postgres) Registration(ctx context.Context, registration httpType.Registration) error {
	text := `INSERT INTO users (email, password) VALUES ($1, $2);`
	_, err := db.conn.Exec(ctx, text, registration.Email, registration.Password)
	if err != nil {
		slog.Error("Failed to insert user into database",
			"module", "postgres",
			"function", "Registration",
			"email", registration.Email,
			"query", text,
			"error", err)
		return err
	}
	slog.Debug("User registered successfully in database",
		"module", "postgres",
		"function", "Registration",
		"email", registration.Email,
		"query", text)
	return nil
}

func (db *Postgres) IsExistUser(ctx context.Context, email string) (bool, error) {
	var idUser id
	text := `SELECT id FROM users WHERE email=$1;`
	err := db.conn.QueryRow(ctx, text, email).Scan(&idUser)
	if err != nil {
		if err.Error() == "no rows in result set" {
			slog.Debug("User not found in database",
				"module", "postgres",
				"function", "IsExistUser",
				"email", email,
				"query", text)
			return false, nil
		}
		slog.Error("Failed to query user existence",
			"module", "postgres",
			"function", "IsExistUser",
			"email", email,
			"query", text,
			"error", err)
		return false, err
	}
	slog.Debug("User found in database",
		"module", "postgres",
		"function", "IsExistUser",
		"email", email,
		"userId", idUser,
		"query", text)
	return true, nil
}

func (db *Postgres) Login(ctx context.Context, login httpType.Login) (string, error) {
	var pswd string
	text := `SELECT password FROM users WHERE email=$1;`
	err := db.conn.QueryRow(ctx, text, login.Email).Scan(&pswd)
	if err != nil {
		if err.Error() == "no rows in result set" {
			slog.Debug("Login failed - user not found",
				"module", "postgres",
				"function", "Login",
				"email", login.Email,
				"query", text)
			return pswd, nil
		}
		slog.Error("Failed to query user login",
			"module", "postgres",
			"function", "Login",
			"email", login.Email,
			"query", text,
			"error", err)
		return pswd, err
	}
	slog.Debug("Login successful",
		"module", "postgres",
		"function", "Login",
		"email", login.Email,
		"query", text)
	return pswd, nil
}

func (db *Postgres) ChangePassword(ctx context.Context, user httpType.ChangePassword) error {
	text := `UPDATE users SET password=$1 WHERE email=$2;`
	_, err := db.conn.Exec(ctx, text, user.Password, user.Email)
	if err != nil {
		slog.Error("Failed to update user password",
			"module", "postgres",
			"function", "ChangePassword",
			"email", user.Email,
			"query", text,
			"error", err)
		return err
	}
	slog.Debug("User password updated successfully",
		"module", "postgres",
		"function", "ChangePassword",
		"email", user.Email,
		"query", text)
	return nil
}

func (db *Postgres) DeleteUser(ctx context.Context, email string) error {
	text := `DELETE FROM users WHERE email=$1;`
	_, err := db.conn.Exec(ctx, text, email)
	if err != nil {
		slog.Error("Failed to delete user from database",
			"module", "postgres",
			"function", "DeleteUser",
			"email", email,
			"query", text,
			"error", err)
		return err
	}
	slog.Debug("User deleted successfully from database",
		"module", "postgres",
		"function", "DeleteUser",
		"email", email,
		"query", text)
	return nil
}
