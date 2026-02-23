package postgres

import (
	"context"
	"fmt"
	"os"

	"github.com/jackc/pgx/v5/pgxpool"
)

type Postgres struct {
	pull *pgxpool.Pool
}

func (db *Postgres) createConnectPath() (string, error) {
	var dbParam [5]string
	for i, param := range []string{"DB_USER", "DB_PASSWORD", "DB_HOST", "DB_PORT", "DB_NAME"} {
		value := os.Getenv(param)
		if value == "" {
			return "", fmt.Errorf("environment variable %s is not set", param)
		}
		dbParam[i] = value
	}
	dbURL := fmt.Sprintf(
		"postgres://%s:%s@%s:%s/%s?sslmode=disable",
		dbParam[0],
		dbParam[1],
		dbParam[2],
		dbParam[3],
		dbParam[4],
	)
	return dbURL, nil
}

func NewDB() (*Postgres, error) {
	db := &Postgres{}
	path, err := db.createConnectPath()
	if err != nil {
		return nil, err
	}
	conf, err := pgxpool.ParseConfig(path)
	if err != nil {
		return nil, err
	}
	conf.MaxConns = 30
	conf.MinConns = 10
	db.pull, err = pgxpool.NewWithConfig(context.Background(), conf)
	if err != nil {
		return nil, err
	}
	return db, nil
}

func (db *Postgres) CloseDB() {
	db.pull.Close()
}
