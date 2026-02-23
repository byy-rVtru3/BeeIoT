package redis

import (
	"BeeIOT/internal/domain/models/httpType"
	"context"
	"testing"
	"time"

	miniredis "github.com/alicebob/miniredis/v2"
	goredis "github.com/redis/go-redis/v9"
)

// helper: create Redis instance backed by miniredis
func newTestRedis(t *testing.T) (*Redis, *miniredis.Miniredis) {
	t.Helper()
	m, err := miniredis.Run()
	if err != nil {
		t.Fatalf("failed to start miniredis: %v", err)
	}

	r := &Redis{rds: goredis.NewClient(&goredis.Options{Addr: m.Addr()})}
	return r, m
}

func TestNotifications_SetAndGet(t *testing.T) {
	rds, m := newTestRedis(t)
	defer m.Close()
	ctx := context.Background()

	note := httpType.NotificationData{Text: "hello", NameHive: "H1", Date: time.Now().Unix()}

	if err := rds.SetNotification(ctx, "user1", note); err != nil {
		t.Fatalf("SetNotification failed: %v", err)
	}
	if err := rds.SetNotification(ctx, "user1", note); err != nil {
		t.Fatalf("SetNotification failed: %v", err)
	}

	notes, err := rds.GetNotifications(ctx, "user1")
	if err != nil {
		t.Fatalf("GetNotifications failed: %v", err)
	}
	if len(notes) != 2 {
		t.Fatalf("expected 2 notifications, got %d", len(notes))
	}

	// after GetNotifications the key should be deleted
	if m.Exists("notifications:user1") {
		t.Fatalf("expected notifications key to be deleted after GetNotifications")
	}
}

func TestJwt_SetExistDelete_DeleteAll(t *testing.T) {
	rds, m := newTestRedis(t)
	defer m.Close()
	ctx := context.Background()

	token := "tok1"
	if err := rds.SetJwt(ctx, "user2", token); err != nil {
		t.Fatalf("SetJwt failed: %v", err)
	}

	ex, err := rds.ExistJwt(ctx, "user2", token)
	if err != nil {
		t.Fatalf("ExistJwt failed: %v", err)
	}
	if !ex {
		t.Fatalf("expected token to exist")
	}

	if err := rds.DeleteJwt(ctx, "user2", token); err != nil {
		t.Fatalf("DeleteJwt failed: %v", err)
	}

	ex, err = rds.ExistJwt(ctx, "user2", token)
	if err != nil {
		t.Fatalf("ExistJwt failed after delete: %v", err)
	}
	if ex {
		t.Fatalf("expected token to be deleted")
	}

	// test DeleteAllJwts
	if err := rds.SetJwt(ctx, "user2", "a"); err != nil {
		t.Fatalf("SetJwt failed: %v", err)
	}
	if err := rds.SetJwt(ctx, "user2", "b"); err != nil {
		t.Fatalf("SetJwt failed: %v", err)
	}
	if err := rds.DeleteAllJwts(ctx, "user2"); err != nil {
		t.Fatalf("DeleteAllJwts failed: %v", err)
	}

	ex, err = rds.ExistJwt(ctx, "user2", "a")
	if err != nil {
		t.Fatalf("ExistJwt failed after DeleteAllJwts: %v", err)
	}
	if ex {
		t.Fatalf("expected no tokens after DeleteAllJwts")
	}

	// ensure underlying key removed in miniredis
	if m.Exists("whitelist:user2") {
		t.Fatalf("expected whitelist:user2 to be removed")
	}
}

func TestSensors_SetExistUpdateGetDelete(t *testing.T) {
	rds, m := newTestRedis(t)
	defer m.Close()
	ctx := context.Background()

	sid := "s123"
	if err := rds.SetSensor(ctx, sid); err != nil {
		t.Fatalf("SetSensor failed: %v", err)
	}

	ex, err := rds.ExistSensor(ctx, sid)
	if err != nil {
		t.Fatalf("ExistSensor failed: %v", err)
	}
	if !ex {
		t.Fatalf("expected sensor to exist after SetSensor")
	}

	// UpdateSensorTimestamp should fail for non-existent sensor
	if err := rds.UpdateSensorTimestamp(ctx, "noexist", time.Now().Unix()); err == nil {
		t.Fatalf("expected error when updating non-existent sensor")
	}

	// Update existent
	now := time.Now().Unix()
	if err := rds.UpdateSensorTimestamp(ctx, sid, now); err != nil {
		t.Fatalf("UpdateSensorTimestamp failed: %v", err)
	}

	all, err := rds.GetAllSensors(ctx)
	if err != nil {
		t.Fatalf("GetAllSensors failed: %v", err)
	}
	val, ok := all[sid]
	if !ok {
		t.Fatalf("expected sensor %s in GetAllSensors", sid)
	}
	if val != now {
		t.Fatalf("expected timestamp %d, got %d", now, val)
	}

	if err := rds.DeleteSensor(ctx, sid); err != nil {
		t.Fatalf("DeleteSensor failed: %v", err)
	}

	ex, err = rds.ExistSensor(ctx, sid)
	if err != nil {
		t.Fatalf("ExistSensor failed after delete: %v", err)
	}
	if ex {
		t.Fatalf("expected sensor to be deleted")
	}

	// ensure underlying hash does not contain the sensor via public API
	allAfter, err := rds.GetAllSensors(ctx)
	if err != nil {
		t.Fatalf("GetAllSensors failed after delete: %v", err)
	}
	if _, ok := allAfter[sid]; ok {
		t.Fatalf("expected sensor %s to be absent after delete", sid)
	}
}

func TestGetAllSensors_ParseError(t *testing.T) {
	rds, m := newTestRedis(t)
	defer m.Close()
	ctx := context.Background()

	// inject non-integer value directly into miniredis hash
	m.HSet("sensors", "bad", "notint")

	_, err := rds.GetAllSensors(ctx)
	if err == nil {
		t.Fatalf("expected parse error when sensor timestamp is not integer")
	}
}
