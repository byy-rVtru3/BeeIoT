package mqtt

import (
	"BeeIOT/internal/domain/models/httpType"
	"BeeIOT/internal/domain/models/mqttTypes"
	"context"
	"encoding/json"
	"fmt"
	"log/slog"
	"strings"
	"time"

	mqtt "github.com/eclipse/paho.mqtt.golang"
)

// handleDeviceData обработчик топика /device/{id}/data
func (m *MQTTClient) handleDeviceData(client mqtt.Client, msg mqtt.Message) {
	topic := msg.Topic()
	// topic = "/device/sensor-id/data"
	sensorId := strings.TrimPrefix(topic, "/device/")
	sensorId = strings.TrimSuffix(sensorId, "/data")

	if sensorId == "" || sensorId == topic {
		slog.Error("Failed to parse topic in data", "topic", topic)
		return
	}

	var data mqttTypes.DeviceData
	err := json.Unmarshal(msg.Payload(), &data)
	if err != nil {
		slog.Error("Failed to unmarshal device data", "sensorId", sensorId, "error", err)
		return
	}
	ctx, cancel := context.WithTimeout(context.Background(), 5*time.Second)
	defer cancel()
	exist, err := m.inMemDb.ExistSensor(ctx, sensorId)
	if err != nil {
		slog.Error("Failed to check sensor existence in DB", "sensorId", sensorId, "error", err)
		return
	}
	if !exist {
		slog.Error("Sensor does not exist in DB", "sensorId", sensorId)
		return
	}
	err = m.inMemDb.UpdateSensorTimestamp(ctx, sensorId, time.Now().Unix())
	if err != nil {
		slog.Error("Failed to update sensor timestamp in DB", "sensorId", sensorId, "error", err)
		return
	}
	email, hiveName, err := m.db.GetEmailHiveBySensorID(context.Background(), sensorId)
	if err != nil {
		slog.Error("Failed to get email and hive by sensor ID", "sensorId", sensorId, "error", err)
		return
	}
	err = m.addNoise(email, hiveName, data)
	if err != nil {
		slog.Error("Failed to add noise data", "sensorId", sensorId, "error", err)
	}
	err = m.addTemperature(email, hiveName, data)
	if err != nil {
		slog.Error("Failed to add temperature data", "sensorId", sensorId, "error", err)
	}
}

func (m *MQTTClient) addNoise(email, hiveName string, data mqttTypes.DeviceData) error {
	if data.Noise != -1 {
		err := m.db.NewNoise(context.Background(), httpType.NoiseLevel{
			Level: data.Noise,
			Time:  time.Unix(data.NoiseTime, 0),
			Email: email,
			Hive:  hiveName,
		})
		if err != nil {
			return err
		}
	}
	return nil
}

func (m *MQTTClient) addTemperature(email, hiveName string, data mqttTypes.DeviceData) error {
	if data.Temperature != -1 {
		err := m.db.NewTemperature(context.Background(), httpType.Temperature{
			Temperature: data.Temperature,
			Time:        time.Unix(data.TemperatureTime, 0),
			Email:       email,
			Hive:        hiveName,
		})
		if err != nil {
			return err
		}
	}
	return nil
}

// handleDeviceStatus обработчик топика /device/{id}/status
func (m *MQTTClient) handleDeviceStatus(client mqtt.Client, msg mqtt.Message) {
	topic := msg.Topic()
	// topic = "/device/sensor-id/status"
	sensorId := strings.TrimPrefix(topic, "/device/")
	sensorId = strings.TrimSuffix(sensorId, "/status")

	if sensorId == "" || sensorId == topic {
		slog.Error("Failed to parse topic in status", "topic", topic)
		return
	}

	var DeviceStatus mqttTypes.DeviceStatus
	err := json.Unmarshal(msg.Payload(), &DeviceStatus)
	if err != nil {
		slog.Error("Failed to unmarshal device status", "sensorId", sensorId, "error", err)
		return
	}
	m.handlingStatusData(DeviceStatus, sensorId)
}

// SendConfig отправляет конфигурацию датчику через топик /device/{id}/config
func (m *MQTTClient) SendConfig(deviceID string, config mqttTypes.DeviceConfig) error {
	topic := fmt.Sprintf("/device/%s/config", deviceID)
	err := m.publishJSON(m.client, topic, 1, false, config)
	if err != nil {
		return fmt.Errorf("failed to publish config to device %s: %w", deviceID, err)
	}
	return nil
}

func (m *MQTTClient) publishJSON(client mqtt.Client, topic string, qos byte, retained bool, v any) error {
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

func (m *MQTTClient) handlingStatusData(data mqttTypes.DeviceStatus, sensorId string) {
	ctx, cancel := context.WithTimeout(context.Background(), 5*time.Second)
	defer cancel()
	exist, err := m.inMemDb.ExistSensor(ctx, sensorId)
	if err != nil {
		slog.Error("Failed to check sensor existence in DB", "sensorId", sensorId, "error", err)
		return
	}
	if !exist {
		err = m.inMemDb.SetSensor(ctx, sensorId)
		if err != nil {
			slog.Error("Failed to add new sensor to DB", "sensorId", sensorId, "error", err)
			return
		}
	}
	err = m.inMemDb.UpdateSensorTimestamp(ctx, sensorId, data.Timestamp)
	if err != nil {
		slog.Error("Failed to update sensor timestamp in DB", "sensorId", sensorId, "error", err)
		return
	}
	err = m.checkBatteryLevel(ctx, sensorId, data)
	if err != nil {
		slog.Error("Failed to check battery level", "sensorId", sensorId, "error", err)
	}
	err = m.checkSignalStrength(ctx, sensorId, data)
	if err != nil {
		slog.Error("Failed to check signal strength", "sensorId", sensorId, "error", err)
	}
	err = m.checkErrors(ctx, sensorId, data)
	if err != nil {
		slog.Error("Failed to check device errors", "sensorId", sensorId, "error", err)
	}
}

func (m *MQTTClient) checkBatteryLevel(ctx context.Context, sensorId string, data mqttTypes.DeviceStatus) error {
	if data.BatteryLevel < batteryLowThreshold {
		email, hive, err := m.db.GetEmailHiveBySensorID(ctx, sensorId)
		if err != nil {
			return err
		}
		err = m.inMemDb.SetNotification(ctx, email, httpType.NotificationData{
			Text:     fmt.Sprintf("Низкий уровень заряда батареи (%d%%) в улье %s", data.BatteryLevel, hive),
			NameHive: hive,
			Date:     data.Timestamp,
		})
		if err != nil {
			return err
		}
	}
	return nil
}

func (m *MQTTClient) checkSignalStrength(ctx context.Context, sensorId string, data mqttTypes.DeviceStatus) error {
	if data.SignalStrength < signalLowThreshold {
		email, hive, err := m.db.GetEmailHiveBySensorID(ctx, sensorId)
		if err != nil {
			return err
		}
		err = m.inMemDb.SetNotification(ctx, email, httpType.NotificationData{
			Text:     fmt.Sprintf("Низкий уровень сигнала (%d%%) в улье %s", data.SignalStrength, hive),
			NameHive: hive,
			Date:     data.Timestamp,
		})
		if err != nil {
			return err
		}
	}
	return nil
}

func (m *MQTTClient) checkErrors(ctx context.Context, sensorId string, data mqttTypes.DeviceStatus) error {
	if len(data.Errors) > 0 {
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
	}
	return nil
}
