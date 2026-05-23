package handlers

import (
	"BeeIOT/internal/domain/models/mqttTypes"
	"encoding/json"
	"net/http"
	"time"
)

func (h *Handler) MQTTSendConfig(w http.ResponseWriter, r *http.Request) {
	var data struct {
		Sensor string                 `json:"sensor"`
		Config mqttTypes.DeviceConfig `json:"config"`
	}
	if err := h.readBodyJSON(w, r, &data); err != nil {
		return
	}
	if data.Sensor == "" {
		http.Error(w, "Имя датчика обязательно", http.StatusBadRequest)
		return
	}

	h.logger.Info().Str("sensor", data.Sensor).Msg("sending MQTT config")
	if err := h.mqtt.SendConfig(data.Sensor, data.Config); err != nil {
		h.logger.Error().Err(err).Str("sensor", data.Sensor).Msg("failed to send MQTT config")
		http.Error(w, "Ошибка отправки конфигурации", http.StatusInternalServerError)
		return
	}
	h.writeBodyJSON(w, "Конфигурация успешно отправлена", nil)
}

func (h *Handler) MQTTSendHealthCheck(w http.ResponseWriter, r *http.Request) {
	var data struct {
		Sensor string `json:"sensor"`
	}
	if err := h.readBodyJSON(w, r, &data); err != nil {
		return
	}
	if data.Sensor == "" {
		http.Error(w, "Имя датчика обязательно", http.StatusBadRequest)
		return
	}

	// Get current cached status timestamp to detect fresh response
	var beforeTs int64
	if cached, err := h.inMemDb.GetLastDeviceStatus(r.Context(), data.Sensor); err == nil && cached != "" {
		var s mqttTypes.DeviceStatus
		if json.Unmarshal([]byte(cached), &s) == nil {
			beforeTs = s.Timestamp
		}
	}

	// Send health check in goroutine (fire-and-forget).
	// MQTT client.Publish is context-independent: even if the HTTP request
	// times out, the message stays in the MQTT outbound queue and will be
	// delivered when the sensor wakes from hibernation (QoS 1).
	h.logger.Info().Str("sensor", data.Sensor).Msg("sending MQTT health check")
	conf := mqttTypes.NewDeviceConfig()
	conf.Health = true
	go func() {
		if err := h.mqtt.SendConfig(data.Sensor, conf); err != nil {
			h.logger.Warn().Err(err).Str("sensor", data.Sensor).Msg("health check publish may be delayed")
		}
	}()

	// Poll Redis for a fresh DeviceStatus (4 s timeout, leaving 1 s buffer
	// for the 5 s chi Timeout middleware).
	ticker := time.NewTicker(300 * time.Millisecond)
	defer ticker.Stop()
	timeout := time.After(4 * time.Second)

	for {
		select {
		case <-timeout:
			http.Error(w, "Датчик не ответил на health check в отведённое время", http.StatusGatewayTimeout)
			return
		case <-ticker.C:
			cached, err := h.inMemDb.GetLastDeviceStatus(r.Context(), data.Sensor)
			if err != nil || cached == "" {
				continue
			}
			var status mqttTypes.DeviceStatus
			if err := json.Unmarshal([]byte(cached), &status); err != nil {
				continue
			}
			if status.Timestamp > beforeTs {
				h.writeBodyJSON(w, "Health check выполнен успешно", status)
				return
			}
		case <-r.Context().Done():
			return
		}
	}
}

func (h *Handler) GetNoiseAndTemp(w http.ResponseWriter, r *http.Request) {
	sensor := r.URL.Query().Get("sensor")
	if sensor == "" {
		http.Error(w, "параметр sensor обязателен", http.StatusBadRequest)
		return
	}

	email, nameHive, err := h.db.GetEmailHiveBySensorID(r.Context(), sensor)
	if err != nil {
		h.logger.Error().Err(err).Str("sensor", sensor).Msg("failed to get email and hive ID")
		http.Error(w, "внутренняя ошибка сервера", http.StatusInternalServerError)
		return
	}
	hive, err := h.db.GetHiveByName(r.Context(), email, nameHive, nil)
	if err != nil {
		h.logger.Error().Err(err).Str("sensor", sensor).Msg("failed to get hive by name")
		http.Error(w, "внутренняя ошибка сервера", http.StatusInternalServerError)
		return
	}
	noise, err := h.db.GetNoiseSinceDay(r.Context(), hive.Id, hive.DateNoise)
	if err != nil {
		h.logger.Error().Err(err).Str("sensor", sensor).Msg("failed to get noise data")
		http.Error(w, "внутренняя ошибка сервера", http.StatusInternalServerError)
		return
	}
	temp, err := h.db.GetTemperaturesSinceTimeById(r.Context(), hive.Id, hive.DateTemperature)
	if err != nil {
		h.logger.Error().Err(err).Str("sensor", sensor).Msg("failed to get temperature data")
		http.Error(w, "внутренняя ошибка сервера", http.StatusInternalServerError)
		return
	}
	resp := struct {
		Noise map[int64][]float64 `json:"noise"`
		Temp  []float64           `json:"temp"`
	}{
		Noise: make(map[int64][]float64),
		Temp:  make([]float64, len(temp)),
	}
	for t, v := range noise {
		for _, val := range v {
			resp.Noise[t.UnixNano()] = append(resp.Noise[t.UnixNano()], val.Level)
		}
	}
	for i, n := range temp {
		resp.Temp[i] = n.Temperature
	}
	h.writeBodyJSON(w, "Data retrieved successfully", resp)
}
