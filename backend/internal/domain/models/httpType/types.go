// Package httpType могут подаваться в функции базы данных, но не могут возвращаться ими
// нужны, чтобы парсить данные с тела запроса и одним параметром передавать их в бд
package httpType

import "time"

type Registration struct {
	Email    string `json:"email"`
	Password string `json:"password"`
	Name     string `json:"name"`
}

type Login struct {
	Email    string `json:"email"`
	Password string `json:"password"`
}

type UserInfo struct {
	Email string `json:"email"`
	Name  string `json:"name"`
}

type Confirm struct {
	Email    string `json:"email"`
	Code     string `json:"code"`
	Password string `json:"password"`
}

type ChangePassword struct {
	Email    string `json:"email"`
	Password string `json:"password"`
}

type ChangeName struct {
	Name string `json:"name"`
}

type NotificationData struct {
	Text     string `json:"text"`
	NameHive string `json:"name"`
	Date     int64  `json:"date"`
}

type QueenRequest struct {
	StartDate string `json:"start_date"`
}

type NoiseLevel struct {
	Level float64   `json:"level"`
	Time  time.Time `json:"time"`
	Email string    `json:"email"`
	Hub   string    `json:"hub"`
}

type HubWeight struct {
	Weight float64   `json:"weight"`
	Time   time.Time `json:"time"`
	Email  string    `json:"email"`
	Hub    string    `json:"hub"`
}

type Temperature struct {
	Temperature float64   `json:"temperature"`
	Time        time.Time `json:"time"`
	Email       string    `json:"email"`
	Hub         string    `json:"hub"`
}

type Hive struct {
	Email    string `json:"email"`
	NameHive string `json:"name"`
}

type HiveListItem struct {
	Name   string `json:"name"`
	Sensor string `json:"sensor"`
	Hub    string `json:"hub"`
	Queen  string `json:"queen"`
}

type HiveDetails struct {
	Name   string `json:"name"`
	Active bool   `json:"active"`
	Sensor string `json:"sensor"`
	Hub    string `json:"hub"`
	Queen  string `json:"queen"`
}

type CreateHive struct {
	Name   string `json:"name"`
	Sensor string `json:"sensor,omitempty"`
}

type ArchiveHive struct {
	Name   string `json:"name"`
	Active bool   `json:"active"`
}

type UpdateHive struct {
	OldName string  `json:"old_name"`
	NewName *string `json:"new_name,omitempty"`
	Active  *bool   `json:"active"`
	Sensor  *string `json:"sensor,omitempty"`
}

type DeleteHive struct {
	Name string `json:"name"`
}

type TelemetryDataPoint struct {
	Time  int64   `json:"time"`
	Value float64 `json:"value"`
}

type LastSensorReading struct {
	Temperature     float64 `json:"temperature"`
	TemperatureTime int64   `json:"temperature_time"`
	Noise           float64 `json:"noise"`
	NoiseTime       int64   `json:"noise_time"`
	Weight          float64 `json:"weight"`
	WeightTime      int64   `json:"weight_time"`
}

type CreateHub struct {
	ID   string `json:"id"`
	Name string `json:"name"`
}

type HubListItem struct {
	ID   string `json:"id"`
	Name string `json:"name"`
}

type HubDetails struct {
	ID   string `json:"id"`
	Name string `json:"name"`
}

type UpdateHub struct {
	ID   string  `json:"id"`
	Name *string `json:"name,omitempty"`
}

type DeleteHub struct {
	ID string `json:"id"`
}

type CreateQueen struct {
	Name      string `json:"name"`
	StartDate string `json:"start_date"`
}

type QueenListItem struct {
	Name      string `json:"name"`
	StartDate string `json:"start_date"`
}

type QueenDetails struct {
	Name     string      `json:"name"`
	Calendar interface{} `json:"calendar"`
}

type UpdateQueen struct {
	OldName   string  `json:"old_name"`
	NewName   *string `json:"new_name,omitempty"`
	StartDate *string `json:"start_date,omitempty"`
}

type LinkToHiveRequest struct {
	HiveName   string `json:"hive_name"`
	TargetName string `json:"target_name"`
}

type CreateTaskRequest struct {
	HiveName    string `json:"hive_name"`
	Title       string `json:"title"`
	Description string `json:"description,omitempty"`
}

type UpdateTaskRequest struct {
	ID          string  `json:"id"`
	Title       *string `json:"title,omitempty"`
	Description *string `json:"description,omitempty"`
}

type DeleteTaskRequest struct {
	ID string `json:"id"`
}

type TaskItem struct {
	ID          string `json:"id"`
	HiveName    string `json:"hive_name"`
	Title       string `json:"title"`
	Description string `json:"description"`
	CreatedAt   int64  `json:"created_at"`
}
