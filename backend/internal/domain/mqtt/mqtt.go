package mqtt

import (
	"BeeIOT/internal/domain/interfaces"
	"errors"
	"fmt"
	"os"
	"time"

	mqtt "github.com/eclipse/paho.mqtt.golang"
	"github.com/rs/zerolog"
)

type Client struct {
	client  mqtt.Client
	inMemDb interfaces.InMemoryDB
	db      interfaces.DB
	logger  zerolog.Logger
}

func NewMQTTClient(db interfaces.DB, inMemDb interfaces.InMemoryDB, logger zerolog.Logger) (*Client, error) {
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

	mqttClient := &Client{inMemDb: inMemDb, db: db, logger: logger}

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
		opts = opts.SetUsername(username)
	}
	if password != "" {
		opts = opts.SetPassword(password)
	}

	mqttClient.client = mqtt.NewClient(opts)

	if token := mqttClient.client.Connect(); token.Wait() && (token.Error() != nil) {
		return nil, fmt.Errorf("failed to connect to MQTT broker: %w", token.Error())
	}

	logger.Info().Str("broker", fmt.Sprintf("%s:%s", host, port)).
		Msg("MQTT client connected successfully")

	return mqttClient, nil
}

func (m *Client) onConnect(_ mqtt.Client) {
	m.logger.Info().Msg("MQTT client connected to broker")

	m.SubscribeToTopics()
}

func (m *Client) onConnectionLost(_ mqtt.Client, err error) {
	m.logger.Error().Err(err).Msg("MQTT connection lost")
}

func (m *Client) SubscribeToTopics() {
	token := m.client.Subscribe("/device/+/data", 1, m.handleDeviceDataTest)
	if token.Wait() && token.Error() != nil {
		m.logger.Error().Err(token.Error()).Str("topic", "data").Msg("failed to subscribe to topic")
	} else {
		m.logger.Info().Str("topic", "data").Msg("subscribed to topic successfully")
	}
	token = m.client.Subscribe("/device/+/status", 1, m.handleDeviceStatusTest)
	if token.Wait() && token.Error() != nil {
		m.logger.Error().Err(token.Error()).Str("topic", "status").Msg("failed to subscribe to topic")
	} else {
		m.logger.Info().Str("topic", "status").Msg("subscribed to topic successfully")
	}
}

// IsConnected проверяет, подключен ли клиент
func (m *Client) IsConnected() bool {
	return m.client.IsConnected()
}

// Disconnect отключается от MQTT брокера
func (m *Client) Disconnect() {
	m.logger.Info().Msg("MQTT client disconnecting")
	m.client.Disconnect(250)
}

// Close закрывает соединение
func (m *Client) Close() error {
	m.Disconnect()
	return nil
}
