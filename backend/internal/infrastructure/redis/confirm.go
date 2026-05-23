package redis

import (
	"BeeIOT/internal/domain/interfaces"
	"context"
	"encoding/base64"
	"fmt"
	"strings"
	"time"
)

func (r *Redis) AddCode(ctx context.Context, email, code, password, name string, timeLive time.Duration) error {
	nameB64 := base64.StdEncoding.EncodeToString([]byte(name))
	return r.rds.Set(ctx, email, code+" "+password+" "+nameB64, timeLive).Err()
}

func (r *Redis) GetPassword(ctx context.Context, email string) (interfaces.CodeData, interfaces.PasswordData, string, error) {
	val, err := r.rds.Get(ctx, email).Result()
	if err != nil {
		return "", "", "", err
	}
	parts := strings.Split(val, " ")
	if len(parts) < 2 {
		return "", "", "", fmt.Errorf("invalid data format")
	}
	name := ""
	if len(parts) >= 3 {
		nameBytes, err := base64.StdEncoding.DecodeString(parts[2])
		if err == nil {
			name = string(nameBytes)
		}
	}
	return parts[0], parts[1], name, nil
}
