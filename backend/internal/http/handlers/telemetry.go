package handlers

import (
	"BeeIOT/internal/domain/models/dbTypes"
	"BeeIOT/internal/domain/models/httpType"
	"BeeIOT/internal/domain/models/mqttTypes"
	"encoding/json"
	"net/http"
	"strconv"
	"time"
)

func (h *Handler) SetHiveWeight(w http.ResponseWriter, r *http.Request) {
	email, err := h.getEmailFromContext(w, r)
	if err != nil {
		return
	}

	var weight httpType.HiveWeight // не нравится мне
	err = h.readBodyJSON(w, r, &weight)
	if err != nil {
		return
	}
	weight.Email = email

	err = h.db.NewHiveWeight(r.Context(), weight)
	if err != nil {
		h.logger.Error().Err(err).Str("email", email).Msg("failed to add weight")
		http.Error(w, "Внутренняя ошибка сервера", http.StatusInternalServerError)
		return
	}

	h.writeBodyJSON(w, "Данные веса успешно установлены", nil)
}

func (h *Handler) DeleteHiveWeight(w http.ResponseWriter, r *http.Request) {
	email, err := h.getEmailFromContext(w, r)
	if err != nil {
		return
	}

	var weight httpType.HiveWeight // не нравится мне
	if err = h.readBodyJSON(w, r, &weight); err != nil {
		return
	}
	weight.Email = email

	err = h.db.DeleteHiveWeight(r.Context(), weight)
	if err != nil {
		h.logger.Error().Err(err).Str("email", email).Msg("failed to delete weight")
		http.Error(w, "Внутренняя ошибка сервера", http.StatusInternalServerError)
		return
	}

	h.writeBodyJSON(w, "Вес удален успешно", nil)
}

func (h *Handler) GetWeightSinceTime(w http.ResponseWriter, r *http.Request) {
	email, err := h.getEmailFromContext(w, r)
	if err != nil {
		return
	}

	hiveName := r.URL.Query().Get("name")
	if hiveName == "" {
		h.logger.Warn().Str("email", email).Msg("missing query param 'name'")
		http.Error(w, "Параметр \"name\" обязателен", http.StatusBadRequest)
		return
	}

	since, ok := parseSince(r.URL.Query().Get("since"))
	if !ok {
		h.logger.Warn().Str("email", email).Str("since", r.URL.Query().Get("since")).Msg("invalid since")
		http.Error(w, "Неверный параметр since (ожидается Unix timestamp)", http.StatusBadRequest)
		return
	}

	weights, err := h.db.GetWeightSinceTime(r.Context(), httpType.Hive{Email: email, NameHive: hiveName}, since)
	if err != nil {
		h.logger.Error().Err(err).Str("email", email).Str("hive", hiveName).Msg("failed to get weight data")
		http.Error(w, "Внутренняя ошибка сервера", http.StatusInternalServerError)
		return
	}

	response := make([]httpType.TelemetryDataPoint, len(weights))
	for i, wt := range weights {
		response[i] = httpType.TelemetryDataPoint{Time: wt.Date.Unix(), Value: wt.Weight}
	}

	h.writeBodyJSON(w, "Данные веса успешно получены", response)
}

func (h *Handler) GetNoiseSinceTime(w http.ResponseWriter, r *http.Request) {
	email, err := h.getEmailFromContext(w, r)
	if err != nil {
		return
	}

	hiveName := r.URL.Query().Get("name")
	if hiveName == "" {
		h.logger.Warn().Str("email", email).Msg("missing query param 'name'")
		http.Error(w, "Параметр \"name\" обязателен", http.StatusBadRequest)
		return
	}

	since, ok := parseSince(r.URL.Query().Get("since"))
	if !ok {
		h.logger.Warn().Str("email", email).Str("since", r.URL.Query().Get("since")).Msg("invalid since")
		http.Error(w, "Неверный параметр since (ожидается Unix timestamp)", http.StatusBadRequest)
		return
	}

	noiseLevels, err := h.db.GetNoiseSinceTime(r.Context(), email, hiveName, since)
	if err != nil {
		h.logger.Error().Err(err).Str("email", email).Str("hive", hiveName).Msg("failed to get noise data")
		http.Error(w, "Внутренняя ошибка сервера", http.StatusInternalServerError)
		return
	}

	response := make([]httpType.TelemetryDataPoint, len(noiseLevels))
	for i, n := range noiseLevels {
		response[i] = httpType.TelemetryDataPoint{Time: n.Date.Unix(), Value: n.Level}
	}

	h.writeBodyJSON(w, "Данные шума успешно получены", response)
}

func (h *Handler) GetTemperatureSinceTime(w http.ResponseWriter, r *http.Request) {
	email, err := h.getEmailFromContext(w, r)
	if err != nil {
		return
	}

	hiveName := r.URL.Query().Get("name")
	if hiveName == "" {
		h.logger.Warn().Str("email", email).Msg("missing query param 'name'")
		http.Error(w, "Параметр \"name\" обязателен", http.StatusBadRequest)
		return
	}

	since, ok := parseSince(r.URL.Query().Get("since"))
	if !ok {
		h.logger.Warn().Str("email", email).Str("since", r.URL.Query().Get("since")).Msg("invalid since")
		http.Error(w, "Неверный параметр since (ожидается Unix timestamp)", http.StatusBadRequest)
		return
	}

	temperatures, err := h.db.GetTemperaturesSinceTime(r.Context(), dbTypes.Hive{Email: email, NameHive: hiveName}, since)
	if err != nil {
		h.logger.Error().Err(err).Str("email", email).Str("hive", hiveName).Msg("failed to get temperature data")
		http.Error(w, "Внутренняя ошибка сервера", http.StatusInternalServerError)
		return
	}

	response := make([]httpType.TelemetryDataPoint, len(temperatures))
	for i, t := range temperatures {
		response[i] = httpType.TelemetryDataPoint{Time: t.Date.Unix(), Value: t.Temperature}
	}

	h.writeBodyJSON(w, "Данные температуры успешно получены", response)
}
func parseSince(sinceStr string) (time.Time, bool) {
	if sinceStr == "" {
		return time.Now().AddDate(0, 0, -1), true
	}
	ts, err := strconv.ParseInt(sinceStr, 10, 64)
	if err != nil || ts < 0 {
		return time.Time{}, false
	}
	return time.Unix(ts, 0), true
}

func (h *Handler) GetLastSensorReading(w http.ResponseWriter, r *http.Request) {
	email, err := h.getEmailFromContext(w, r)
	if err != nil {
		return
	}

	hiveName := r.URL.Query().Get("name")
	if hiveName == "" {
		h.logger.Warn().Str("email", email).Msg("missing query param 'name'")
		http.Error(w, "Параметр \"name\" обязателен", http.StatusBadRequest)
		return
	}

	hive, err := h.db.GetHiveByName(r.Context(), email, hiveName, nil)
	if err != nil {
		h.logger.Error().Err(err).Str("email", email).Str("hive", hiveName).Msg("hive not found")
		http.Error(w, "Улей не найден", http.StatusNotFound)
		return
	}

	if hive.SensorID == "" {
		http.Error(w, "К данному улью не привязан датчик", http.StatusNotFound)
		return
	}

	data, err := h.inMemDb.GetLastSensorData(r.Context(), hive.SensorID)
	if err != nil {
		h.logger.Warn().Err(err).Str("sensor", hive.SensorID).Msg("no last sensor data")
		http.Error(w, "Нет данных от датчика", http.StatusNotFound)
		return
	}

	var sensorData mqttTypes.DeviceData
	if err := json.Unmarshal([]byte(data), &sensorData); err != nil {
		h.logger.Error().Err(err).Str("sensor", hive.SensorID).Msg("failed to unmarshal sensor data")
		http.Error(w, "Внутренняя ошибка сервера", http.StatusInternalServerError)
		return
	}

	lastReading := httpType.LastSensorReading{
		Temperature:     sensorData.Temperature,
		TemperatureTime: sensorData.TemperatureTime,
		Noise:           sensorData.Noise,
		NoiseTime:       sensorData.NoiseTime,
	}

	h.writeBodyJSON(w, "Последние данные датчика получены", lastReading)
}
