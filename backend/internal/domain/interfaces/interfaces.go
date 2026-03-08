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
	ChangeNameUser(ctx context.Context, email string, name string) error

	NewHive(ctx context.Context, email, nameHive string) error
	GetHives(ctx context.Context, email string) ([]dbTypes.Hive, error)
	GetHiveByName(ctx context.Context, email, nameHive string) (dbTypes.Hive, error)
	DeleteHive(ctx context.Context, email, nameHive string) error
	UpdateHive(ctx context.Context, email, oldName, newName string) error
	UpdateHiveTemperatureCheck(ctx context.Context, hiveId int, t time.Time) error
	UpdateHiveNoiseCheck(ctx context.Context, hiveId int, t time.Time) error
	GetEmailHiveBySensorID(ctx context.Context, sensorID string) (string, string, error)

	NewTemperature(ctx context.Context, temp httpType.Temperature) error
	GetTemperaturesSinceTime(ctx context.Context, hive dbTypes.Hive, time time.Time) ([]dbTypes.HivesTemperatureData, error)
	GetTemperaturesSinceTimeById(
		ctx context.Context, hiveId int, time time.Time) ([]dbTypes.HivesTemperatureData, error)

	NewNoise(ctx context.Context, noise httpType.NoiseLevel) error
	GetNoiseSinceTime(
		ctx context.Context, email, nameHive string, time time.Time) ([]dbTypes.HivesNoiseData, error)
	GetNoiseSinceDay(
		ctx context.Context, id int, date time.Time) (map[time.Time][]dbTypes.HivesNoiseData, error)

	NewHiveWeight(ctx context.Context, weight httpType.HiveWeight) error
	DeleteHiveWeight(ctx context.Context, weight httpType.HiveWeight) error
	GetWeightSinceTime(ctx context.Context, hive httpType.Hive, time time.Time) ([]dbTypes.HivesWeightData, error)
}

type InMemoryDB interface {
	SetNotification(ctx context.Context, email string, note httpType.NotificationData) error
	GetNotifications(ctx context.Context, email string) ([]httpType.NotificationData, error)
	SetJwt(ctx context.Context, email, token string) error
	ExistJwt(ctx context.Context, email, jwtId string) (bool, error)
	DeleteJwt(ctx context.Context, email, jwtId string) error
	DeleteAllJwts(ctx context.Context, email string) error
	SetSensor(ctx context.Context, sensorID string) error
	UpdateSensorTimestamp(ctx context.Context, sensorID string, timestamp int64) error
	ExistSensor(ctx context.Context, sensorID string) (bool, error)
	GetAllSensors(ctx context.Context) (map[string]int64, error)
	DeleteSensor(ctx context.Context, sensorID string) error
}

type PasswordData = string
type CodeData = string

type PasswordKeeper interface {
	AddCode(ctx context.Context, email, code, password string, timeLive time.Duration) error
	GetPassword(ctx context.Context, email string) (CodeData, PasswordData, error)
}
