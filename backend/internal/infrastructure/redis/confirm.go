package redis

import (
	"BeeIOT/internal/domain/interfaces"
	"context"
	"fmt"
	"time"
)

func (r *Redis) AddCode(ctx context.Context, email, code, password string, timeLive time.Duration) error {
	return r.rds.Set(ctx, email, code+" "+password, timeLive).Err()
}

func (r *Redis) GetPassword(ctx context.Context, email string) (interfaces.CodeData, interfaces.PasswordData, error) {
	val, err := r.rds.Get(ctx, email).Result()
	if err != nil {
		return "", "", err
	}
	var code, password string
	n, err := fmt.Sscanf(val, "%s %s", &code, &password)
	if err != nil || n != 2 {
		return "", "", fmt.Errorf("invalid data format")
	}
	return code, password, nil
}
