package mqtt

import (
	"BeeIOT/internal/domain/models/httpType"
	"BeeIOT/internal/domain/models/mqttTypes"
	"context"
	"encoding/json"
	"fmt"
	"strings"
	"time"

	mqtt "github.com/eclipse/paho.mqtt.golang"
)

func (m *Client) handleDeviceDataTest(_ mqtt.Client, msg mqtt.Message) {
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
	fmt.Println("\nReceived test data:", data, "\nFrom sensor:", sensorId)
	m.logger.Info().Msgf("Received test data from sensor %s: %+v", sensorId, data)
}

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
	email, hiveName, err := m.db.GetEmailHiveBySensorID(ctx, sensorId)
	if err != nil {
		m.logger.Error().Err(err).Str("topic", topic).Msg("Failed to get hive name")
		return
	}
	if err := m.addNoise(ctx, email, hiveName, data); err != nil {
		m.logger.Error().Err(err).Str("topic", topic).Msg("Failed to add noise")
	}
	if err := m.addTemperature(ctx, email, hiveName, data); err != nil {
		m.logger.Error().Err(err).Str("topic", topic).Msg("Failed to add temperature")
	}
}

func (m *Client) addNoise(ctx context.Context, email, hiveName string, data mqttTypes.DeviceData) error {
	if data.Noise == -1 {
		return nil
	}
	err := m.db.NewNoise(ctx, httpType.NoiseLevel{
		Level: data.Noise,
		Time:  time.Unix(data.NoiseTime, 0),
		Email: email,
		Hive:  hiveName,
	})
	return err
}

func (m *Client) addTemperature(ctx context.Context, email, hiveName string, data mqttTypes.DeviceData) error {
	if data.Temperature == -1 {
		return nil
	}
	err := m.db.NewTemperature(ctx, httpType.Temperature{
		Temperature: data.Temperature,
		Time:        time.Unix(data.TemperatureTime, 0),
		Email:       email,
		Hive:        hiveName,
	})
	return err
}

func (m *Client) handleDeviceStatusTest(_ mqtt.Client, msg mqtt.Message) {
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
	fmt.Println("\nReceived test status:", DeviceStatus, "\nFrom sensor:", sensorId)
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
	batteryLowThreshold = 20
	signalLowThreshold  = 10
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
	err = m.inMemDb.SetNotification(ctx, email, httpType.NotificationData{
		Text:     fmt.Sprintf("Низкий уровень заряда батареи (%d%%) в улье %s", data.BatteryLevel, hive),
		NameHive: hive,
		Date:     data.Timestamp,
	})
	return err
}

func (m *Client) checkSignalStrength(ctx context.Context, sensorId string, data mqttTypes.DeviceStatus) error {
	if data.SignalStrength >= signalLowThreshold {
		return nil
	}
	email, hive, err := m.db.GetEmailHiveBySensorID(ctx, sensorId)
	if err != nil {
		return err
	}
	err = m.inMemDb.SetNotification(ctx, email, httpType.NotificationData{
		Text:     fmt.Sprintf("Низкий уровень сигнала (%d%%) в улье %s", data.SignalStrength, hive),
		NameHive: hive,
		Date:     data.Timestamp,
	})
	return err
}

func (m *Client) checkErrors(ctx context.Context, sensorId string, data mqttTypes.DeviceStatus) error {
	if len(data.Errors) == 0 {
		return nil
	}
	email, hive, err := m.db.GetEmailHiveBySensorID(ctx, sensorId)
	if err != nil {
		return err
	}
	for _, errorMsg := range data.Errors {
		err = m.inMemDb.SetNotification(ctx, email, httpType.NotificationData{
			Text:     fmt.Sprintf("Ошибка от датчика в улье %s: %s", hive, errorMsg),
			NameHive: hive,
			Date:     data.Timestamp,
		})
		if err != nil {
			return err
		}
	}
	return nil
}
