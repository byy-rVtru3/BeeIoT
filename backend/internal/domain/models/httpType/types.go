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
