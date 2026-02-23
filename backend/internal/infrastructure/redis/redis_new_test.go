package redis

import (
	"os"
	"testing"
)

func TestNewRedis_MissingEnv(t *testing.T) {
	oldAddr := os.Getenv("REDIS_ADDR")
	oldPass := os.Getenv("REDIS_PASSWORD")
	oldDB := os.Getenv("REDIS_DB")
	defer func() {
		_ = os.Setenv("REDIS_ADDR", oldAddr)
		_ = os.Setenv("REDIS_PASSWORD", oldPass)
		_ = os.Setenv("REDIS_DB", oldDB)
	}()
	_ = os.Unsetenv("REDIS_ADDR")
	_ = os.Unsetenv("REDIS_PASSWORD")
	_ = os.Unsetenv("REDIS_DB")

	_, err := NewRedis()
	if err == nil {
		t.Fatalf("expected error when redis env vars are missing")
	}
}

func TestNewRedis_SuccessAndClose(t *testing.T) {
	oldAddr := os.Getenv("REDIS_ADDR")
	oldPass := os.Getenv("REDIS_PASSWORD")
	oldDB := os.Getenv("REDIS_DB")
	defer func() {
		_ = os.Setenv("REDIS_ADDR", oldAddr)
		_ = os.Setenv("REDIS_PASSWORD", oldPass)
		_ = os.Setenv("REDIS_DB", oldDB)
	}()
	_ = os.Setenv("REDIS_ADDR", "127.0.0.1:6379")
	_ = os.Setenv("REDIS_PASSWORD", "")
	_ = os.Setenv("REDIS_DB", "0")

	r, err := NewRedis()
	if err != nil {
		t.Fatalf("NewRedis returned error: %v", err)
	}
	if r == nil {
		t.Fatalf("expected non-nil Redis instance")
	}
	if err := r.Close(); err != nil {
		t.Fatalf("Close returned error: %v", err)
	}
}
