package interfaces

import (
	"BeeIOT/internal/domain/models/dbTypes"
	"BeeIOT/internal/domain/models/httpType"
	"context"
	"time"
)

type ConfirmSender interface {
	SendConfirmationCode(toEmail, code string) error
}

type DB interface {
	Registration(ctx context.Context, registration httpType.Registration) error
	IsExistUser(ctx context.Context, email string) (bool, error)
	Login(ctx context.Context, login httpType.Login) (string, error)
	ChangePassword(ctx context.Context, user httpType.ChangePassword) error
	DeleteUser(ctx context.Context, email string) error
	GetUserById(ctx context.Context, id int) (string, error)

	NewHive(ctx context.Context, email, nameHive string) error
	GetHives(ctx context.Context, email string) ([]dbTypes.Hive, error)
	GetHiveByName(ctx context.Context, email, nameHive string) (dbTypes.Hive, error)
	DeleteHive(ctx context.Context, email, nameHive string) error
	UpdateHive(ctx context.Context, nameHive string, hive dbTypes.Hive) error
	GetTemperaturesSinceTimeById(
		ctx context.Context, hiveId int, time time.Time) ([]dbTypes.HivesTemperatureData, error)

	GetNoiseSinceTimeMap(
		ctx context.Context, id int, date time.Time) (map[time.Time][]dbTypes.HivesNoiseData, error)
}

type InMemoryDB interface {
	SetNotification(ctx context.Context, email string, note httpType.NotificationData) error
	GetNotifications(ctx context.Context, email string) ([]httpType.NotificationData, error)
	SetJwt(ctx context.Context, email, token string) error
	ExistJwt(ctx context.Context, email, jwtId string) (bool, error)
	DeleteJwt(ctx context.Context, email, jwtId string) error
	DeleteAllJwts(ctx context.Context, email string) error
}
