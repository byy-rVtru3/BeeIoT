// Package dbTypes используются в качестве возвращаемых значений функциями для работы с бд
// не могут использоваться для парсинга тела http запроса
package dbTypes

import (
	"time"
)

type Hive struct { // вот эту пиздень надо изменить, чтобы она всю инфу о улье хранила
	Id              int       `json:"id"`
	NameHive        string    `json:"name"`
	Email           string    `json:"email"`
	DateTemperature time.Time `json:"temperature_check"`
	DateNoise       time.Time `json:"noise_check"`
}

type Task struct {
	Name  string    `json:"name"`
	Time  time.Time `json:"time"`
	Email string    `json:"email"`
	Hive  string    `json:"hive"`
}

type HivesTemperatureData struct {
	Id          int       `json:"id"`
	Date        time.Time `json:"temperature_check"`
	Temperature float64   `json:"temperature"`
}

type HivesNoiseData struct {
	Date  time.Time `json:"noise_check"`
	Level float64   `json:"level"`
}
