package httpType

import "time"

type Hive struct { // вот эту пиздень надо изменить, чтобы она всю инфу о улье хранила
	Id              int       `json:"id"`
	NameHive        string    `json:"name"`
	Email           string    `json:"email"`
	DateTemperature time.Time `json:"temperature_check"`
}

type Task struct {
	Name  string    `json:"name"`
	Time  time.Time `json:"time"`
	Email string    `json:"email"`
	Hive  string    `json:"hive"`
}

type Registration struct {
	Email    string `json:"email"`
	Password string `json:"password"`
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

// 3 таблицы нижние полная залупа
// Таблица 1 - уровень шума
type NoiseLevel struct {
	Level int       `json:"level"`
	Time  time.Time `json:"time"`
	Email string    `json:"email"`
	Hive  string    `json:"hive"`
}

// Таблица 2 - вес улья
type HiveWeight struct {
	Weight float64   `json:"weight"`
	Time   time.Time `json:"time"`
	Email  string    `json:"email"`
	Hive   string    `json:"hive"`
}

// Таблица 3 - температура
type Temperature struct {
	Temperature float64   `json:"temperature"`
	Time        time.Time `json:"time"`
	Email       string    `json:"email"`
	Hive        string    `json:"hive"`
}

type QueenRequest struct {
	StartDate string `json:"start_date"`
}

type HivesTemperatureData struct {
	Id          int       `json:"id"`
	Date        time.Time `json:"temperature_check"`
	Temperature float64   `json:"temperature"`
}
