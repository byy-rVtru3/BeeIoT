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
	Hive  string    `json:"hive"`
}

type HiveWeight struct {
	Weight float64   `json:"weight"`
	Time   time.Time `json:"time"`
	Email  string    `json:"email"`
	Hive   string    `json:"hive"`
}

type Temperature struct {
	Temperature float64   `json:"temperature"`
	Time        time.Time `json:"time"`
	Email       string    `json:"email"`
	Hive        string    `json:"hive"`
}

type Hive struct {
	Email    string `json:"email"`
	NameHive string `json:"name"`
}

type HiveResponse struct {
	Id              int    `json:"id"`
	NameHive        string `json:"name"`
	Email           string `json:"email"`
	DateTemperature string `json:"date_temperature"`
	DateNoise       string `json:"date_noise"`
	SensorID        string `json:"sensor_id"`
	Status          bool   `json:"status"`
}

type CreateHive struct {
	Name string `json:"name"`
}

type UpdateHive struct {
	OldName string `json:"old_name"`
	NewName string `json:"new_name"`
}

type DeleteHive struct {
	Name string `json:"name"`
}

type NoiseLevelResponse struct {
	Date  time.Time `json:"date"`
	Level float64   `json:"level"`
}

type WeightResponse struct {
	Date   time.Time `json:"date"`
	Weight float64   `json:"weight"`
}

type TemperatureResponse struct {
	Date        time.Time `json:"date"`
	Temperature float64   `json:"temperature"`
}
