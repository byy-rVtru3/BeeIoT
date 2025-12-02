package mqtt

import (
	"BeeIOT/internal/domain/interfaces"
	"errors"
	"fmt"
	"log/slog"
	"os"
	"time"

	mqtt "github.com/eclipse/paho.mqtt.golang"
)

type MQTTClient struct {
	client  mqtt.Client
	inMemDb interfaces.InMemoryDB
	db      interfaces.DB
}

func NewMQTTClient(db interfaces.DB, inMemDb interfaces.InMemoryDB) (*MQTTClient, error) {
	host := os.Getenv("MQTT_HOST")
	port := os.Getenv("MQTT_PORT")
	username := os.Getenv("MQTT_USERNAME")
	password := os.Getenv("MQTT_PASSWORD")

	if host == "" {
		return nil, errors.New("MQTT_HOST environment variable is not set")
	}
	if port == "" {
		return nil, errors.New("MQTT_PORT environment variable is not set")
	}

	mqttClient := &MQTTClient{inMemDb: inMemDb, db: db}

	opts := mqtt.NewClientOptions().
		AddBroker(fmt.Sprintf("tcp://%s:%s", host, port)).
		SetClientID("beeiot_server").
		SetCleanSession(true).
		SetMaxReconnectInterval(30 * time.Second).
		SetWriteTimeout(15 * time.Second).
		SetAutoReconnect(true).
		SetKeepAlive(60 * time.Second).
		SetPingTimeout(10 * time.Second).
		SetConnectionLostHandler(mqttClient.onConnectionLost).
		SetOnConnectHandler(mqttClient.onConnect)

	if username != "" {
		opts.SetUsername(username)
	}
	if password != "" {
		opts.SetPassword(password)
	}

	mqttClient.client = mqtt.NewClient(opts)

	if token := mqttClient.client.Connect(); token.Wait() && token.Error() != nil {
		return nil, fmt.Errorf("failed to connect to MQTT broker: %w", token.Error())
	}

	slog.Info("MQTT client connected successfully", "broker", fmt.Sprintf("%s:%s", host, port))

	return mqttClient, nil
}

func (m *MQTTClient) onConnect(client mqtt.Client) {
	slog.Info("MQTT client connected to broker")

	m.SubscribeToTopics()
}

func (m *MQTTClient) onConnectionLost(client mqtt.Client, err error) {
	slog.Error("MQTT connection lost", "error", err)
}

func (m *MQTTClient) SubscribeToTopics() {
	if token := m.client.Subscribe("/device/+/data", 1, m.handleDeviceData); token.Wait() && token.Error() != nil {
		slog.Error("Failed to subscribe to data topics", "error", token.Error())
	} else {
		slog.Info("Subscribed to data topics", "pattern", "/device/+/data")
	}

	if token := m.client.Subscribe("/device/+/status", 1, m.handleDeviceStatus); token.Wait() && token.Error() != nil {
		slog.Error("Failed to subscribe to status topics", "error", token.Error())
	} else {
		slog.Info("Subscribed to status topics", "pattern", "/device/+/status")
	}
}

// IsConnected проверяет, подключен ли клиент
func (m *MQTTClient) IsConnected() bool {
	return m.client.IsConnected()
}

// Disconnect отключается от MQTT брокера
func (m *MQTTClient) Disconnect() {
	slog.Info("Disconnecting MQTT client")
	m.client.Disconnect(250)
}

// Close закрывает соединение
func (m *MQTTClient) Close() error {
	m.Disconnect()
	return nil
}
