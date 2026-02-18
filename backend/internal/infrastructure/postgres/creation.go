package postgres

import (
	"context"
	"fmt"
	"os"

	"github.com/jackc/pgx/v5/pgxpool"
)

type Postgres struct {
	pull    *pgxpool.Pool
	errChan chan<- error
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
		return db, err
	}
	db.pull, err = pgxpool.New(context.Background(), path)
	if err != nil {
		return db, err
	}
	return db, nil
}

func (db *Postgres) CloseDB() {
	db.pull.Close()
}
