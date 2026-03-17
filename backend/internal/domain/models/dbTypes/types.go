// Package dbTypes используются в качестве возвращаемых значений функциями для работы с бд
// не могут использоваться для парсинга тела http запроса
package dbTypes

import (
	"time"
)

type Hive struct {
	Id              int
	NameHive        string
	Email           string
	DateTemperature time.Time
	DateNoise       time.Time
	SensorID        string
	Status          bool
	HubID           *int
	QueenID         *int
	HubName         string
	QueenName       string
}

type Hub struct {
	Id      int
	NameHub string
	Email   string
	Sensor  string
}

type Queen struct {
	Id         int
	Email      string
	Name       string
	StartDate  time.Time
	DateEnd    time.Time
	IsBreeding bool
}

type Task struct {
	Name  string
	Time  time.Time
	Email string
	Hive  string
}

type HivesTemperatureData struct {
	Date        time.Time
	Temperature float64
}

type HivesNoiseData struct {
	Date  time.Time
	Level float64
}

type HivesWeightData struct {
	Weight float64
	Date   time.Time
}
