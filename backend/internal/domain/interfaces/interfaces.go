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
	GetUserByEmail(ctx context.Context, email string) (string, string, error)
	ChangeNameUser(ctx context.Context, email string, name string) error

	NewHive(ctx context.Context, email, nameHive, sensorName string) error
	GetHives(ctx context.Context, email string, active *bool) ([]dbTypes.Hive, error)
	GetHiveByName(ctx context.Context, email, nameHive string, active *bool) (dbTypes.Hive, error)
	DeleteHive(ctx context.Context, email, nameHive string) error
	UpdateHive(ctx context.Context, email string, data httpType.UpdateHive) error
	UpdateHiveStatus(ctx context.Context, email, name string, status bool) error
	UpdateHiveTemperatureCheck(ctx context.Context, hiveId int, t time.Time) error
	UpdateHiveNoiseCheck(ctx context.Context, hiveId int, t time.Time) error
	GetEmailHiveBySensorID(ctx context.Context, sensorID string) (string, string, error)
	LinkHubToHive(ctx context.Context, email, hiveName, hubName string) error
	LinkQueenToHive(ctx context.Context, email, hiveName, queenName string) error

	NewHub(ctx context.Context, email, nameHub, sensorName string) error
	GetHubs(ctx context.Context, email string) ([]dbTypes.Hub, error)
	GetHubBySensor(ctx context.Context, email, sensor string) (dbTypes.Hub, error)
	GetHubSensorByHive(ctx context.Context, email, hiveName string) (string, error)
	GetEmailByHubSensor(ctx context.Context, hubSensor string) (string, error)
	DeleteHub(ctx context.Context, email, nameHub string) error
	UpdateHub(ctx context.Context, email string, data httpType.UpdateHub) error

	NewQueen(ctx context.Context, email, name, startDate string) error
	GetQueens(ctx context.Context, email string) ([]dbTypes.Queen, error)
	GetQueenByName(ctx context.Context, email, name string) (dbTypes.Queen, error)
	DeleteQueen(ctx context.Context, email, name string) error
	UpdateQueen(ctx context.Context, email string, data httpType.UpdateQueen) error

	NewTemperature(ctx context.Context, temp httpType.Temperature) error
	GetTemperaturesSinceTime(ctx context.Context, email, hub string, time time.Time) ([]dbTypes.HivesTemperatureData, error)
	GetTemperaturesSinceTimeById(ctx context.Context, hubId int, time time.Time) ([]dbTypes.HivesTemperatureData, error)

	NewNoise(ctx context.Context, noise httpType.NoiseLevel) error
	GetNoiseSinceTime(ctx context.Context, email, hub string, time time.Time) ([]dbTypes.HivesNoiseData, error)
	GetNoiseSinceDay(ctx context.Context, hubId int, date time.Time) (map[time.Time][]dbTypes.HivesNoiseData, error)

	NewHiveWeight(ctx context.Context, weight httpType.HubWeight) error
	DeleteHiveWeight(ctx context.Context, weight httpType.HubWeight) error
	GetWeightSinceTime(ctx context.Context, email, hub string, time time.Time) ([]dbTypes.HivesWeightData, error)

	SetFirebaseToken(ctx context.Context, email, device, fcm string) error
	GetFirebaseToken(ctx context.Context, email string) ([]string, error)
	DeleteFirebaseToken(ctx context.Context, email string, badFcm []string) error

	CreateTask(ctx context.Context, email string, req httpType.CreateTaskRequest) (string, error)
	GetTasks(ctx context.Context, email, hiveName string) ([]dbTypes.Task, error)
	UpdateTask(ctx context.Context, email string, req httpType.UpdateTaskRequest) error
	DeleteTask(ctx context.Context, email, taskID string) error
	GetTaskByID(ctx context.Context, taskID string) (dbTypes.Task, error)
}

type InMemoryDB interface {
	SetJwt(ctx context.Context, email, token string) error
	ExistJwt(ctx context.Context, email, jwtId string) (bool, error)
	DeleteJwt(ctx context.Context, email, jwtId string) error
	DeleteAllJwts(ctx context.Context, email string) error
	SetSensor(ctx context.Context, sensorID string) error
	UpdateSensorTimestamp(ctx context.Context, sensorID string, timestamp int64) error
	ExistSensor(ctx context.Context, sensorID string) (bool, error)
	GetAllSensors(ctx context.Context) (map[string]int64, error)
	DeleteSensor(ctx context.Context, sensorID string) error
	SetLastSensorData(ctx context.Context, sensorID string, data string) error
	GetLastSensorData(ctx context.Context, sensorID string) (string, error)
	SetLastDeviceStatus(ctx context.Context, sensorID string, data string) error
	GetLastDeviceStatus(ctx context.Context, sensorID string) (string, error)
}

type PasswordData = string
type CodeData = string

type PasswordKeeper interface {
	AddCode(ctx context.Context, email, code, password, name string, timeLive time.Duration) error
	GetPassword(ctx context.Context, email string) (CodeData, PasswordData, string, error)
}
