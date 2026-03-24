package mqtt

import (
	"BeeIOT/internal/domain/models/httpType"
	"BeeIOT/internal/domain/models/mqttTypes"
	"BeeIOT/internal/domain/notification"
	"context"
	"encoding/json"
	"errors"
	"fmt"
	"strings"
	"time"

	mqtt "github.com/eclipse/paho.mqtt.golang"
)

// handleDeviceData обработчик топика /device/{id}/data
func (m *Client) handleDeviceData(_ mqtt.Client, msg mqtt.Message) {
	topic := msg.Topic()
	parts := strings.Split(topic, "/")
	if len(parts) != 4 || parts[1] != "device" || parts[3] != "data" {
		m.logger.Error().Str("topic", topic).Msg("Invalid topic format")
		return
	}
	sensorId := parts[2]

	var data mqttTypes.DeviceData
	if err := json.Unmarshal(msg.Payload(), &data); err != nil {
		m.logger.Error().Err(err).Str("topic", topic).Msg("Failed to unmarshal payload")
		return
	}

	m.logger.Info().
		Str("sensor", sensorId).
		Float64("temperature", data.Temperature).
		Float64("noise", data.Noise).
		Float64("weight", data.Weight).
		Msg("Received device data")

	ctx, cancel := context.WithTimeout(context.Background(), 5*time.Second)
	defer cancel()

	exist, err := m.inMemDb.ExistSensor(ctx, sensorId)
	if err != nil {
		m.logger.Error().Err(err).Str("topic", topic).Msg("Failed to check existence of sensor")
		return
	}
	if !exist {
		m.logger.Error().Err(err).Str("topic", topic).Msg("Sensor does not exist")
		return
	}
	if err := m.inMemDb.UpdateSensorTimestamp(ctx, sensorId, time.Now().Unix()); err != nil {
		m.logger.Error().Err(err).Str("topic", topic).Msg("Failed to update timestamp")
		return
	}
	// Cache last sensor data for quick retrieval, preserving weight from existing cache
	cachePayload := msg.Payload()
	if existing, err := m.inMemDb.GetLastSensorData(ctx, sensorId); err == nil {
		var cached mqttTypes.DeviceData
		if json.Unmarshal([]byte(existing), &cached) == nil && cached.WeightTime != 0 {
			// Прошивка не шлёт вес — сохраняем его из кеша
			data.Weight = cached.Weight
			data.WeightTime = cached.WeightTime
			if b, err := json.Marshal(data); err == nil {
				cachePayload = b
			}
		}
	}
	if err := m.inMemDb.SetLastSensorData(ctx, sensorId, string(cachePayload)); err != nil {
		m.logger.Error().Err(err).Str("topic", topic).Msg("Failed to cache last sensor data")
	}
	email, hiveName, err := m.db.GetEmailHiveBySensorID(ctx, sensorId)
	if err != nil {
		// Датчик не привязан к улью — пробуем найти email через hub напрямую
		m.logger.Warn().Str("topic", topic).Str("sensor", sensorId).Msg("Sensor not linked to hive, trying hub lookup")
		email, err = m.db.GetEmailByHubSensor(ctx, sensorId)
		if err != nil {
			m.logger.Error().Err(err).Str("topic", topic).Msg("Failed to find hub by sensor ID")
			return
		}
		if err := m.addNoise(ctx, email, sensorId, data); err != nil {
			m.logger.Error().Err(err).Str("topic", topic).Msg("Failed to add noise")
		}
		if err := m.addTemperature(ctx, email, sensorId, data); err != nil {
			m.logger.Error().Err(err).Str("topic", topic).Msg("Failed to add temperature")
		}
		if err := m.addWeight(ctx, email, sensorId, data); err != nil {
			m.logger.Error().Err(err).Str("topic", topic).Msg("Failed to add weight")
		}
		return
	}
	if err := m.checkNoiseLevel(ctx, email, hiveName, data); err != nil {
		m.logger.Error().Err(err).Str("topic", topic).Msg("Failed to check noise level")
	}
	hubSensor, err := m.db.GetHubSensorByHive(ctx, email, hiveName)
	if err != nil {
		m.logger.Warn().Err(err).Str("topic", topic).Str("hive", hiveName).Msg("Hive has no hub, skipping telemetry storage")
		return
	}
	if err := m.addNoise(ctx, email, hubSensor, data); err != nil {
		m.logger.Error().Err(err).Str("topic", topic).Msg("Failed to add noise")
	}
	if err := m.addTemperature(ctx, email, hubSensor, data); err != nil {
		m.logger.Error().Err(err).Str("topic", topic).Msg("Failed to add temperature")
	}
	if err := m.addWeight(ctx, email, hubSensor, data); err != nil {
		m.logger.Error().Err(err).Str("topic", topic).Msg("Failed to add weight")
	}
}

func (m *Client) addNoise(ctx context.Context, email, hubSensor string, data mqttTypes.DeviceData) error {
	if data.Noise == -1 {
		return nil
	}
	return m.db.NewNoise(ctx, httpType.NoiseLevel{
		Level: data.Noise,
		Time:  time.Unix(data.NoiseTime, 0),
		Email: email,
		Hub:   hubSensor,
	})
}

func (m *Client) addTemperature(ctx context.Context, email, hubSensor string, data mqttTypes.DeviceData) error {
	if data.Temperature == -1 {
		return nil
	}
	return m.db.NewTemperature(ctx, httpType.Temperature{
		Temperature: data.Temperature,
		Time:        time.Unix(data.TemperatureTime, 0),
		Email:       email,
		Hub:         hubSensor,
	})
}

func (m *Client) addWeight(ctx context.Context, email, hubSensor string, data mqttTypes.DeviceData) error {
	if data.Weight == -1 || data.WeightTime == 0 {
		return nil
	}
	return m.db.NewHiveWeight(ctx, httpType.HubWeight{
		Weight: data.Weight,
		Time:   time.Unix(data.WeightTime, 0),
		Email:  email,
		Hub:    hubSensor,
	})
}

// handleDeviceStatus обработчик топика /device/{id}/status
func (m *Client) handleDeviceStatus(_ mqtt.Client, msg mqtt.Message) {
	topic := msg.Topic()
	parts := strings.Split(topic, "/")
	if len(parts) != 4 || parts[1] != "device" || parts[3] != "status" {
		m.logger.Error().Str("topic", topic).Msg("Invalid topic format")
		return
	}
	sensorId := parts[2]

	var DeviceStatus mqttTypes.DeviceStatus
	if err := json.Unmarshal(msg.Payload(), &DeviceStatus); err != nil {
		m.logger.Error().Err(err).Str("topic", topic).Msg("Failed to unmarshal payload")
		return
	}

	// Cache device status for health check responses
	if err := m.inMemDb.SetLastDeviceStatus(context.Background(), sensorId, string(msg.Payload())); err != nil {
		m.logger.Warn().Err(err).Str("sensor", sensorId).Msg("Failed to cache device status")
	}

	m.handlingStatusData(DeviceStatus, sensorId)
}

// SendConfig отправляет конфигурацию датчику через топик /device/{id}/config
func (m *Client) SendConfig(deviceID string, config mqttTypes.DeviceConfig) error {
	topic := fmt.Sprintf("/device/%s/config", deviceID)
	if err := m.publishJSON(m.client, topic, 1, false, config); err != nil {
		m.logger.Error().Err(err).Str("topic", topic).Msg("Failed to publish config")
		return fmt.Errorf("failed to publish config to device %s: %w", deviceID, err)
	}
	m.logger.Info().Msgf("Published config to device %s", deviceID)
	return nil
}

func (m *Client) publishJSON(client mqtt.Client, topic string, qos byte, retained bool, v any) error {
	data, err := json.Marshal(v)
	if err != nil {
		return fmt.Errorf("marshal payload: %w", err)
	}

	token := client.Publish(topic, qos, retained, data)

	if ok := token.WaitTimeout(5 * time.Second); !ok {
		return fmt.Errorf("publish timeout")
	}
	if err := token.Error(); err != nil {
		return fmt.Errorf("publish error: %w", err)
	}

	return nil
}

const (
	batteryLowThreshold   = 20
	signalLowThreshold    = 10
	noiseHighThreshold    = 50
	defaultSamplingPeriod = 5 // seconds
)

func (m *Client) handlingStatusData(data mqttTypes.DeviceStatus, sensorId string) {
	ctx, cancel := context.WithTimeout(context.Background(), 5*time.Second)
	defer cancel()
	exist, err := m.inMemDb.ExistSensor(ctx, sensorId)
	if err != nil {
		m.logger.Error().Err(err).Str("sensor", sensorId).Msg("Failed to check existence of sensor")
		return
	}
	if !exist {
		err = m.inMemDb.SetSensor(ctx, sensorId)
		if err != nil {
			m.logger.Error().Err(err).Str("sensor", sensorId).Msg("Failed to set sensor")
			return
		}
		// Новый датчик — отправляем начальный конфиг с интервалом defaultSamplingPeriod сек
		go func() {
			cfg := mqttTypes.DeviceConfig{
				SamplingNoise: defaultSamplingPeriod,
				SamplingTemp:  defaultSamplingPeriod,
				Restart:       false,
				Health:        false,
				Frequency:     defaultSamplingPeriod,
				Delete:        false,
			}
			if sendErr := m.SendConfig(sensorId, cfg); sendErr != nil {
				m.logger.Warn().Err(sendErr).Str("sensor", sensorId).Msg("Failed to send initial config to new sensor")
			} else {
				m.logger.Info().Str("sensor", sensorId).Int("sampling_period_s", defaultSamplingPeriod).Msg("Sent initial config to new sensor")
			}
		}()
	}

	if err = m.inMemDb.UpdateSensorTimestamp(ctx, sensorId, data.Timestamp); err != nil {
		m.logger.Error().Err(err).Str("sensor", sensorId).Msg("Failed to update timestamp")
		return
	}
	if err = m.checkBatteryLevel(ctx, sensorId, data); err != nil {
		m.logger.Error().Err(err).Str("sensor", sensorId).Msg("Failed to check battery level")
	}
	if err = m.checkSignalStrength(ctx, sensorId, data); err != nil {
		m.logger.Error().Err(err).Str("sensor", sensorId).Msg("Failed to check signal level")
	}
	if err = m.checkErrors(ctx, sensorId, data); err != nil {
		m.logger.Error().Err(err).Str("sensor", sensorId).Msg("Failed to check error")
	}
}

func (m *Client) checkBatteryLevel(ctx context.Context, sensorId string, data mqttTypes.DeviceStatus) error {
	if data.BatteryLevel >= batteryLowThreshold {
		return nil
	}
	email, hive, err := m.db.GetEmailHiveBySensorID(ctx, sensorId)
	if err != nil {
		return err
	}
	tokens, err := m.db.GetFirebaseToken(ctx, email)
	if err != nil {
		return err
	}
	if m.notification == nil {
		return nil
	}
	m.logger.Info().Str("sensor", sensorId).Int("battery", data.BatteryLevel).Str("email", email).Msg("Sending low battery notification")
	badToken, err := m.notification.SendNotification(ctx, notification.Data{
		Title:     fmt.Sprintf("Низкий уровень заряда батареи (%d%%) в улье", data.BatteryLevel),
		Body:      "Пожалуйста, замените батарею в ближайшее время, чтобы обеспечить бесперебойную работу датчика.",
		Data:      map[string]string{"hive": hive},
		Tokens:    tokens,
		Important: true,
	})
	switch {
	case errors.Is(err, notification.ErrInvalidTokens):
		err = m.db.DeleteFirebaseToken(ctx, email, badToken)
		return err
	case err != nil:
		return fmt.Errorf("failed to send notification: %w", err)
	}
	m.logger.Info().Str("sensor", sensorId).Msg("Low battery notification sent successfully")
	return nil
}

func (m *Client) checkSignalStrength(ctx context.Context, sensorId string, data mqttTypes.DeviceStatus) error {
	if data.SignalStrength >= signalLowThreshold {
		return nil
	}
	email, hive, err := m.db.GetEmailHiveBySensorID(ctx, sensorId)
	if err != nil {
		return err
	}
	tokens, err := m.db.GetFirebaseToken(ctx, email)
	if err != nil {
		return err
	}
	if m.notification == nil {
		return nil
	}
	m.logger.Info().Str("sensor", sensorId).Int("signal", data.SignalStrength).Str("email", email).Msg("Sending low signal notification")
	badToken, err := m.notification.SendNotification(ctx, notification.Data{
		Title:     fmt.Sprintf("Низкий уровень сигнала (%d%%) в улье", data.SignalStrength),
		Body:      "Пожалуйста, проверьте расположение датчика и убедитесь, что он находится в зоне стабильного сигнала.",
		Data:      map[string]string{"hive": hive},
		Tokens:    tokens,
		Important: true,
	})
	switch {
	case errors.Is(err, notification.ErrInvalidTokens):
		err = m.db.DeleteFirebaseToken(ctx, email, badToken)
		return err
	case err != nil:
		return fmt.Errorf("failed to send notification: %w", err)
	}
	m.logger.Info().Str("sensor", sensorId).Msg("Low signal notification sent successfully")
	return nil
}

func (m *Client) checkErrors(ctx context.Context, sensorId string, data mqttTypes.DeviceStatus) error {
	if len(data.Errors) == 0 {
		return nil
	}
	email, hive, err := m.db.GetEmailHiveBySensorID(ctx, sensorId)
	if err != nil {
		return err
	}
	tokens, err := m.db.GetFirebaseToken(ctx, email)
	if err != nil {
		return err
	}
	if m.notification == nil {
		return nil
	}
	m.logger.Info().Str("sensor", sensorId).Strs("errors", data.Errors).Str("email", email).Msg("Sending device errors notification")
	badToken, err := m.notification.SendNotification(ctx, notification.Data{
		Title: fmt.Sprintf("Ошибки датчика в улье %s", hive),
		Body: fmt.Sprintf("Датчик сообщил об ошибках: %s. Пожалуйста, проверьте состояние датчика.",
			strings.Join(data.Errors, ", ")),
		Data:      map[string]string{"hive": hive},
		Tokens:    tokens,
		Important: true,
	})
	switch {
	case errors.Is(err, notification.ErrInvalidTokens):
		err = m.db.DeleteFirebaseToken(ctx, email, badToken)
		return err
	case err != nil:
		return fmt.Errorf("failed to send notification: %w", err)
	}
	m.logger.Info().Str("sensor", sensorId).Msg("Device errors notification sent successfully")
	return nil
}

// checkNoiseLevel отправляет уведомление приложению, если уровень шума превышает noiseHighThreshold дБ.
func (m *Client) checkNoiseLevel(ctx context.Context, email, hive string, data mqttTypes.DeviceData) error {
	if data.Noise == -1 || data.Noise <= noiseHighThreshold {
		return nil
	}
	tokens, err := m.db.GetFirebaseToken(ctx, email)
	if err != nil {
		return err
	}
	if m.notification == nil {
		return nil
	}
	m.logger.Info().Str("hive", hive).Float64("noise", data.Noise).Str("email", email).Msg("Sending high noise notification")
	badToken, err := m.notification.SendNotification(ctx, notification.Data{
		Title: fmt.Sprintf("Высокий уровень шума в улье %s", hive),
		Body: fmt.Sprintf("Зафиксирован уровень шума %.1f дБ, превышающий допустимый порог (%d дБ). Пожалуйста, проверьте состояние улья.",
			data.Noise, noiseHighThreshold),
		Data:      map[string]string{"hive": hive},
		Tokens:    tokens,
		Important: true,
	})
	switch {
	case errors.Is(err, notification.ErrInvalidTokens):
		err = m.db.DeleteFirebaseToken(ctx, email, badToken)
		return err
	case err != nil:
		return fmt.Errorf("failed to send noise notification: %w", err)
	}
	m.logger.Info().Str("hive", hive).Msg("High noise notification sent successfully")
	return nil
}
