package mqttTypes

// DeviceData представляет данные от датчика (топик /device/{id}/data)
// Структура содержит данные измерений с датчиков улья
type DeviceData struct {
	// Temperature - температура в цельсиях. Значение -1 означает отсутствие данных
	Temperature float64 `json:"temperature"`

	// TemperatureTime - метка времени измерения температуры (UNIX Seconds)
	TemperatureTime int64 `json:"temperature_time"`

	// Noise - уровень шума в децибелах. Значение -1 означает отсутствие данных
	Noise float64 `json:"noise"`

	// NoiseTime - метка времени измерения шума (UNIX Seconds)
	NoiseTime int64 `json:"noise_time"`
}

// DeviceStatus представляет статус датчика (топик /device/{id}/status)
// Структура содержит информацию о состоянии устройства
type DeviceStatus struct {
	// BatteryLevel - уровень заряда батареи от 0 до 100. Значение -1 означает отсутствие данных
	BatteryLevel int `json:"battery_level"`

	// SignalStrength - уровень сигнала от 0 до 100. Значение -1 означает отсутствие данных
	SignalStrength int `json:"signal_strength"`

	// Timestamp - метка времени статуса (UNIX Seconds)
	Timestamp int64 `json:"timestamp"`

	// Errors - массив текстовых описаний ошибок. Пустой массив, если ошибок нет
	Errors []string `json:"errors"`
}

// DeviceConfig представляет конфигурацию для датчика (топик /device/{id}/config)
// Структура содержит параметры настройки устройства, отправляемые сервером
type DeviceConfig struct {
	// SamplingNoise - частота сбора данных о шуме в секундах. Значение -1 означает, что не установлена
	SamplingNoise int `json:"sampling_rate_noise"`

	// SamplingTemp - частота сбора данных о температуре в секундах. Значение -1 означает, что не установлена
	SamplingTemp int `json:"sampling_rate_temperature"`

	// Restart - true, если устройство нужно перезагрузить
	Restart bool `json:"restart_device"`

	// Health - true, если нужно выполнить проверку состояния устройства
	Health bool `json:"health_check"`

	// Frequency - частота отправки статуса в секундах. Значение -1 означает, что не установлена
	Frequency int `json:"frequency_status"`

	// Delete - true, если устройство нужно удалить
	Delete bool `json:"delete_device"`
}

func NewDeviceConfig() DeviceConfig {
	return DeviceConfig{
		SamplingNoise: -1,
		SamplingTemp:  -1,
		Restart:       false,
		Health:        false,
		Frequency:     -1,
		Delete:        false,
	}
}
