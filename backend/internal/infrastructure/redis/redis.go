package redis

import (
	"BeeIOT/internal/domain/types/httpType"
	"errors"
	"os"
	"strconv"

	"github.com/redis/go-redis/v9"
)

type Redis struct {
	rds *redis.Client
}

func NewRedis() (*Redis, error) {
	data := make([]string, 3)
	for i, elem := range []string{"REDIS_ADDR", "REDIS_PASSWORD", "REDIS_DB"} {
		str, ok := os.LookupEnv(elem)
		if !ok {
			return nil, errors.New("environment variable " + elem + " is not set")
		}
		data[i] = str
	}
	numDb, err := strconv.Atoi(data[2])
	if err != nil {
		return nil, err
	}
	return &Redis{redis.NewClient(&redis.Options{
		Addr:     data[0],
		Password: data[1],
		DB:       numDb,
	})}, nil
}

func (r *Redis) Close() error {
	return r.rds.Close()
}

func (r *Redis) Set(email string, data httpType.NotificationData) error {
	return nil
}

func (r *Redis) Get(email string) ([]httpType.NotificationData, error) {
	return nil, nil
}
