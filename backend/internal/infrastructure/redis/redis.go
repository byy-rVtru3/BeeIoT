package redis

import (
	"BeeIOT/internal/domain/types/httpType"
	"context"
	"encoding/json"
	"errors"
	"fmt"
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

func (r *Redis) SetNotification(ctx context.Context, email string, note httpType.NotificationData) error {
	data, err := json.Marshal(note)
	if err != nil {
		return err
	}

	key := fmt.Sprintf("notifications:%s", email)
	if err := r.rds.LPush(ctx, key, data).Err(); err != nil {
		return err
	}
	return nil
}

func (r *Redis) GetNotifications(ctx context.Context, email string) ([]httpType.NotificationData, error) {
	key := fmt.Sprintf("notifications:%s", email)

	vals, err := r.rds.LRange(ctx, key, 0, -1).Result()
	if err != nil {
		return nil, err
	}

	var notifications []httpType.NotificationData
	for _, v := range vals {
		var n httpType.NotificationData
		if err := json.Unmarshal([]byte(v), &n); err == nil {
			notifications = append(notifications, n)
		}
	}
	return notifications, r.deleteNotifications(ctx, email)
}

func (r *Redis) deleteNotifications(ctx context.Context, email string) error {
	err := r.rds.Del(ctx, "notifications:"+email).Err()
	return err
}

func (r *Redis) SetJwt(ctx context.Context, email, token string) error {
	err := r.rds.SAdd(ctx, "whitelist:"+email, token).Err()
	return err
}

func (r *Redis) ExistJwt(ctx context.Context, email, jwt string) (bool, error) {
	exist, err := r.rds.SIsMember(ctx, "whitelist:"+email, jwt).Result()
	return exist, err
}

func (r *Redis) DeleteJwt(ctx context.Context, email, jwt string) error {
	err := r.rds.SRem(ctx, "whitelist:"+email, jwt).Err()
	return err
}

func (r *Redis) DeleteAllJwts(ctx context.Context, email string) error {
	err := r.rds.Del(ctx, "whitelist:"+email).Err()
	return err
}
