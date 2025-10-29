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
