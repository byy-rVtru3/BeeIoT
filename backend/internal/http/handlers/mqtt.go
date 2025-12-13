package handlers

import (
	"BeeIOT/internal/domain/models/mqttTypes"
	"net/http"
)

func (h *Handler) MQTTSendConfig(w http.ResponseWriter, r *http.Request) {
	var data struct {
		Sensor string `json:"sensor"`
	}
	if err := h.readBodyJSON(w, r, &data); err != nil {
		return
	}

	h.logger.Info().Str("sensor", data.Sensor).Msg("sending MQTT config")
	conf := mqttTypes.NewDeviceConfig()
	conf.Health = true
	conf.Frequency = 1
	conf.SamplingTemp = 1
	conf.SamplingNoise = 1
	if err := h.mqtt.SendConfig(data.Sensor, conf); err != nil {
		h.logger.Error().Err(err).Str("sensor", data.Sensor).Msg("failed to send MQTT config")
		http.Error(w, "Failed to send MQTT config", http.StatusInternalServerError)
		return
	}
	h.writeBodyJSON(w, "MQTT config sent successfully", nil)
}

func (h *Handler) GetNoiseAndTemp(w http.ResponseWriter, r *http.Request) {
	var data struct {
		Sensor string `json:"sensor"`
	}
	if err := h.readBodyJSON(w, r, &data); err != nil {
		return
	}

	email, nameHive, err := h.db.GetEmailHiveBySensorID(r.Context(), data.Sensor)
	if err != nil {
		h.logger.Error().Err(err).Str("sensor", data.Sensor).Msg("failed to get email and hive ID")
		http.Error(w, "внутренняя ошибка сервера", http.StatusInternalServerError)
		return
	}
	hive, err := h.db.GetHiveByName(r.Context(), email, nameHive)
	if err != nil {
		h.logger.Error().Err(err).Str("sensor", data.Sensor).Msg("failed to get hive by name")
		http.Error(w, "внутренняя ошибка сервера", http.StatusInternalServerError)
		return
	}
	noise, err := h.db.GetNoiseSinceTimeMap(r.Context(), hive.Id, hive.DateNoise)
	if err != nil {
		h.logger.Error().Err(err).Str("sensor", data.Sensor).Msg("failed to get noise data")
		http.Error(w, "внутренняя ошибка сервера", http.StatusInternalServerError)
		return
	}
	temp, err := h.db.GetTemperaturesSinceTimeById(r.Context(), hive.Id, hive.DateTemperature)
	if err != nil {
		h.logger.Error().Err(err).Str("sensor", data.Sensor).Msg("failed to get temperature data")
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
