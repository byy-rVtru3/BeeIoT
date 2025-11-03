package httpType

type Hive struct {
	NameHive string `json:"name"`
	Email    string `json:"email"`
	Hash     string `json:"hash"`
}

type Task struct {
	Name  string `json:"name"`
	Time  string `json:"time"` // определить формат времени
	Email string `json:"email"`
	Hash  string `json:"hash"`
	Hive  string `json:"hive"`
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
}

// Таблица 1 - уровень шума
type NoiseLevel struct {
	Level int    `json:"level"`
	Time  string `json:"time"`
	Email string `json:"email"`
	Hash  string `json:"hash"`
	Hive  string `json:"hive"`
}

// Таблица 2 - вес улья
type HiveWeight struct {
	Weight float64 `json:"weight"`
	Time   string  `json:"time"`
	Email  string  `json:"email"`
	Hash   string  `json:"hash"`
	Hive   string  `json:"hive"`
}

// Таблица 3 - температура
type Temperature struct {
	Temperature float64 `json:"temperature"`
	Time        string  `json:"time"`
	Email       string  `json:"email"`
	Hash        string  `json:"hash"`
	Hive        string  `json:"hive"`
}

type QueenRequest struct {
	StartDate string `json:"start_date"`
}
type QueenPhaseCalendar struct {
	StartDate string `json:"start_date"`

	EggPhase struct {
		Standing string `json:"standing"`
		Tilted   string `json:"tilted"`
		Lying    string `json:"lying"`
	} `json:"egg_phase"`

	LarvaPhase struct {
		Start  string `json:"start"`
		Day1   string `json:"day_1"`
		Day2   string `json:"day_2"`
		Day3   string `json:"day_3"`
		Day4   string `json:"day_4"`
		Day5   string `json:"day_5"`
		Sealed string `json:"sealed"`
	} `json:"larva_phase"`

	PupaPhase struct {
		Start     string `json:"start"`
		End       string `json:"end"`
		Duration  string `json:"duration"`
		Selection string `json:"selection"`
	} `json:"pupa_phase"`

	QueenPhase struct {
		EmergenceStart      string `json:"emergence_start"`
		EmergenceEnd        string `json:"emergence_end"`
		MaturationStart     string `json:"maturation_start"`
		MaturationEnd       string `json:"maturation_end"`
		MatingFlightStart   string `json:"mating_flight_start"`
		MatingFlightEnd     string `json:"mating_flight_end"`
		InseminationStart   string `json:"insemination_start"`
		InseminationEnd     string `json:"insemination_end"`
		EggLayingCheckStart string `json:"egg_laying_check_start"`
		EggLayingCheckEnd   string `json:"egg_laying_check_end"`
	} `json:"queen_phase"`
}
