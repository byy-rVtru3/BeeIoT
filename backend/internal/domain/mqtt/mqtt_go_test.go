package mqtt

import (
	"fmt"
	"os"
	"testing"

	mqtt "github.com/eclipse/paho.mqtt.golang"
	"github.com/rs/zerolog"
)

// Mock client implementing Subscribe, IsConnected, Disconnect to test mqtt.go behavior
type MockClientWithSubscribe struct {
	mqtt.Client
	Subscribed    []string
	TokenToReturn mqtt.Token
	Connected     bool
	Disconnected  bool
}

func (m *MockClientWithSubscribe) Subscribe(topic string, qos byte, callback mqtt.MessageHandler) mqtt.Token {
	m.Subscribed = append(m.Subscribed, topic)
	return m.TokenToReturn
}

func (m *MockClientWithSubscribe) IsConnected() bool       { return m.Connected }
func (m *MockClientWithSubscribe) Disconnect(quiesce uint) { m.Disconnected = true }

func TestSubscribeToTopics_Success(t *testing.T) {
	// token that reports success
	okToken := &GenericMockToken{waitRet: true, waitTimeoutRet: true, err: nil}
	mc := &MockClientWithSubscribe{TokenToReturn: okToken}
	client := &Client{client: mc, logger: zerolog.Nop()}

	client.SubscribeToTopics()

	if len(mc.Subscribed) != 2 {
		t.Fatalf("expected 2 subscribed topics, got %d", len(mc.Subscribed))
	}
	// check topics contain expected patterns
	foundData := false
	foundStatus := false
	for _, tpc := range mc.Subscribed {
		if tpc == "/device/+/data" {
			foundData = true
		}
		if tpc == "/device/+/status" {
			foundStatus = true
		}
	}
	if !foundData || !foundStatus {
		t.Fatalf("expected both data and status topics to be subscribed, got %v", mc.Subscribed)
	}
}

func TestSubscribeToTopics_Failure(t *testing.T) {
	// token that reports error
	errToken := &GenericMockToken{waitRet: true, waitTimeoutRet: true, err: fmt.Errorf("sub failed")}
	mc := &MockClientWithSubscribe{TokenToReturn: errToken}
	client := &Client{client: mc, logger: zerolog.Nop()}

	client.SubscribeToTopics()

	if len(mc.Subscribed) != 2 {
		t.Fatalf("expected 2 subscribed topics even on error, got %d", len(mc.Subscribed))
	}
}

func TestOnConnect_CallsSubscribe(t *testing.T) {
	okToken := &GenericMockToken{waitRet: true, waitTimeoutRet: true, err: nil}
	mc := &MockClientWithSubscribe{TokenToReturn: okToken}
	client := &Client{client: mc, logger: zerolog.Nop()}

	client.onConnect(nil)

	if len(mc.Subscribed) != 2 {
		t.Fatalf("onConnect should call SubscribeToTopics, subscribed: %v", mc.Subscribed)
	}
}

func TestOnConnectionLost_NoPanic(t *testing.T) {
	client := &Client{logger: zerolog.Nop()}
	// ensure no panic when connection lost handler is called
	client.onConnectionLost(nil, fmt.Errorf("connection lost test"))
}

func TestIsConnected_Disconnect_Close(t *testing.T) {
	mc := &MockClientWithSubscribe{Connected: true}
	client := &Client{client: mc, logger: zerolog.Nop()}

	if !client.IsConnected() {
		t.Fatalf("expected IsConnected true")
	}

	client.Disconnect()
	if !mc.Disconnected {
		t.Fatalf("expected underlying client to be disconnected")
	}

	if err := client.Close(); err != nil {
		t.Fatalf("expected Close to return nil, got %v", err)
	}
}

func TestNewMQTTClient_NoEnv(t *testing.T) {
	// ensure env vars are unset
	oldHost := os.Getenv("MQTT_HOST")
	oldPort := os.Getenv("MQTT_PORT")
	oldUser := os.Getenv("MQTT_USERNAME")
	oldPass := os.Getenv("MQTT_PASSWORD")
	defer func() {
		_ = os.Setenv("MQTT_HOST", oldHost)
		_ = os.Setenv("MQTT_PORT", oldPort)
		_ = os.Setenv("MQTT_USERNAME", oldUser)
		_ = os.Setenv("MQTT_PASSWORD", oldPass)
	}()
	_ = os.Unsetenv("MQTT_HOST")
	_ = os.Unsetenv("MQTT_PORT")
	_ = os.Unsetenv("MQTT_USERNAME")
	_ = os.Unsetenv("MQTT_PASSWORD")

	_, err := NewMQTTClient(nil, nil, nil, zerolog.Nop())
	if err == nil {
		t.Fatalf("expected error when MQTT_HOST/MQTT_PORT not set")
	}
}

func TestNewMQTTClient_EnvConnectFail(t *testing.T) {
	oldHost := os.Getenv("MQTT_HOST")
	oldPort := os.Getenv("MQTT_PORT")
	oldUser := os.Getenv("MQTT_USERNAME")
	oldPass := os.Getenv("MQTT_PASSWORD")
	defer func() {
		_ = os.Setenv("MQTT_HOST", oldHost)
		_ = os.Setenv("MQTT_PORT", oldPort)
		_ = os.Setenv("MQTT_USERNAME", oldUser)
		_ = os.Setenv("MQTT_PASSWORD", oldPass)
	}()

	// set to local address and unlikely port to force connection failure
	_ = os.Setenv("MQTT_HOST", "127.0.0.1")
	_ = os.Setenv("MQTT_PORT", "65535")
	_ = os.Unsetenv("MQTT_USERNAME")
	_ = os.Unsetenv("MQTT_PASSWORD")

	_, err := NewMQTTClient(nil, nil, nil, zerolog.Nop())
	if err == nil {
		t.Fatalf("expected error when MQTT connect fails")
	}
}
