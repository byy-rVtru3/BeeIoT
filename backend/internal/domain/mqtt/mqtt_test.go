package mqtt

import (
	"BeeIOT/internal/domain/interfaces"
	"BeeIOT/internal/domain/models/httpType"
	"BeeIOT/internal/domain/models/mqttTypes"
	"context"
	"encoding/json"
	"fmt"
	"strings"
	"testing"
	"time"

	mqtt "github.com/eclipse/paho.mqtt.golang"
	"github.com/rs/zerolog"
)

// --- Mocks ---

type MockInMemoryDB struct {
	interfaces.InMemoryDB
	ExistSensorResult    bool
	ExistSensorError     error
	SetSensorError       error
	UpdateTimestampError error

	// расширим MockInMemoryDB чтобы захватывать SetSensor вызовы
	SetSensorCall bool
	LastSetSensor string
}

func (m *MockInMemoryDB) ExistSensor(_ context.Context, _ string) (bool, error) {
	return m.ExistSensorResult, m.ExistSensorError
}

func (m *MockInMemoryDB) SetSensor(_ context.Context, sensorID string) error {
	// capture
	if ms, ok := interface{}(m).(*MockInMemoryDB); ok {
		ms.SetSensorCall = true
		ms.LastSetSensor = sensorID
	}
	return m.SetSensorError
}

func (m *MockInMemoryDB) UpdateSensorTimestamp(_ context.Context, _ string, _ int64) error {
	return m.UpdateTimestampError
}

func (m *MockInMemoryDB) SetLastSensorData(_ context.Context, _ string, _ string) error {
	return nil
}

func (m *MockInMemoryDB) SetLastDeviceStatus(_ context.Context, _ string, _ string) error {
	return nil
}

func (m *MockInMemoryDB) GetLastDeviceStatus(_ context.Context, _ string) (string, error) {
	return "", nil
}

type MockDB struct {
	interfaces.DB
	GetEmailHiveBySensorIDResultEmail string
	GetEmailHiveBySensorIDResultHive  string
	GetEmailHiveBySensorIDError       error
	GetHubSensorByHiveResult          string
	GetHubSensorByHiveError           error
	GetEmailByHubSensorResult         string
	GetEmailByHubSensorError          error
	NewNoiseError                     error
	NewTemperatureError               error
	NewHiveWeightError                error
}

func (m *MockDB) GetEmailHiveBySensorID(_ context.Context, _ string) (string, string, error) {
	return m.GetEmailHiveBySensorIDResultEmail, m.GetEmailHiveBySensorIDResultHive, m.GetEmailHiveBySensorIDError
}

func (m *MockDB) GetHubSensorByHive(_ context.Context, _, _ string) (string, error) {
	return m.GetHubSensorByHiveResult, m.GetHubSensorByHiveError
}

func (m *MockDB) GetEmailByHubSensor(_ context.Context, _ string) (string, error) {
	return m.GetEmailByHubSensorResult, m.GetEmailByHubSensorError
}

func (m *MockDB) NewNoise(_ context.Context, _ httpType.NoiseLevel) error {
	return m.NewNoiseError
}

func (m *MockDB) NewTemperature(_ context.Context, _ httpType.Temperature) error {
	return m.NewTemperatureError
}

func (m *MockDB) NewHiveWeight(_ context.Context, _ httpType.HubWeight) error {
	return m.NewHiveWeightError
}

func (m *MockDB) GetFirebaseToken(_ context.Context, _ string) ([]string, error) {
	return nil, nil
}

func (m *MockDB) DeleteFirebaseToken(_ context.Context, _ string, _ []string) error {
	return nil
}

type MockMessage struct {
	topic   string
	payload []byte
}

func (m *MockMessage) Duplicate() bool   { return false }
func (m *MockMessage) Qos() byte         { return 0 }
func (m *MockMessage) Retained() bool    { return false }
func (m *MockMessage) Topic() string     { return m.topic }
func (m *MockMessage) MessageID() uint16 { return 0 }
func (m *MockMessage) Payload() []byte   { return m.payload }
func (m *MockMessage) Ack()              {}

type MockToken struct {
	mqtt.Token
	err error
}

func (t *MockToken) Wait() bool                       { return true }
func (t *MockToken) WaitTimeout(_ time.Duration) bool { return true }
func (t *MockToken) Error() error                     { return t.err }
func (t *MockToken) Done() <-chan struct{} {
	ch := make(chan struct{})
	close(ch)
	return ch
}

// улучшенный MockToken для тестирования разных веток
type GenericMockToken struct {
	waitRet        bool
	waitTimeoutRet bool
	err            error
}

// implement GenericMockToken methods
func (t *GenericMockToken) Wait() bool                       { return t.waitRet }
func (t *GenericMockToken) WaitTimeout(_ time.Duration) bool { return t.waitTimeoutRet }
func (t *GenericMockToken) Error() error                     { return t.err }
func (t *GenericMockToken) Done() <-chan struct{} {
	ch := make(chan struct{})
	close(ch)
	return ch
}

// Mock клиент, возвращающий заданный токен
type MockMqttClientWithToken struct {
	mqtt.Client
	token mqtt.Token
}

func (m *MockMqttClientWithToken) Publish(_ string, _ byte, _ bool, _ interface{}) mqtt.Token {
	return m.token
}

// simple MockMqttClient used in some tests
type MockMqttClient struct {
	mqtt.Client
	PublishError error
}

func (m *MockMqttClient) Publish(_ string, _ byte, _ bool, _ interface{}) mqtt.Token {
	return &MockToken{err: m.PublishError}
}

// --- Tests ---

func TestHandleDeviceData(t *testing.T) {
	logger := zerolog.Nop()
	inMem := &MockInMemoryDB{ExistSensorResult: true}
	db := &MockDB{GetEmailHiveBySensorIDResultEmail: "test@test.com", GetEmailHiveBySensorIDResultHive: "Hive1", GetHubSensorByHiveResult: "sensor123"}

	client := &Client{inMemDb: inMem, db: db, logger: logger}

	// Case 1: Normal data processing
	data := mqttTypes.DeviceData{
		Temperature:     25.5,
		TemperatureTime: time.Now().Unix(),
		Noise:           50.0,
		NoiseTime:       time.Now().Unix(),
	}
	payload, _ := json.Marshal(data)
	msg := &MockMessage{
		topic:   "/device/sensor123/data",
		payload: payload,
	}

	client.handleDeviceData(nil, msg)

	// Since NewNoise/NewTemperature return nil error, just ensure no panic.
	// We can enhance MockDB to capture calls if needed.
}

func TestHandleDeviceData_SensorNotExist(t *testing.T) {
	logger := zerolog.Nop()
	inMem := &MockInMemoryDB{ExistSensorResult: false}
	db := &MockDB{}

	client := &Client{inMemDb: inMem, db: db, logger: logger}

	data := mqttTypes.DeviceData{Temperature: 25.0}
	payload, _ := json.Marshal(data)
	msg := &MockMessage{
		topic:   "/device/sensor123/data",
		payload: payload,
	}

	client.handleDeviceData(nil, msg)
	// Should log error "Sensor does not exist" and return early.
	// We can't strictly assert logs without hooking logger, but coverage will increase.
}

func TestHandleDeviceStatus(t *testing.T) {
	logger := zerolog.Nop()
	inMem := &MockInMemoryDB{ExistSensorResult: true}
	db := &MockDB{GetEmailHiveBySensorIDResultEmail: "test@test.com", GetEmailHiveBySensorIDResultHive: "Hive1"}

	client := &Client{inMemDb: inMem, db: db, logger: logger}

	status := mqttTypes.DeviceStatus{
		BatteryLevel:   15, // Low battery < 20
		SignalStrength: 50,
		Timestamp:      time.Now().Unix(),
		Errors:         []string{},
	}
	payload, _ := json.Marshal(status)
	msg := &MockMessage{
		topic:   "/device/sensor123/status",
		payload: payload,
	}

	client.handleDeviceStatus(nil, msg)

	// With nil notification, checkBatteryLevel returns nil immediately
}

func TestCheckSignalStrength(t *testing.T) {
	logger := zerolog.Nop()
	inMem := &MockInMemoryDB{}
	db := &MockDB{GetEmailHiveBySensorIDResultEmail: "test@test.com", GetEmailHiveBySensorIDResultHive: "Hive1"}
	client := &Client{inMemDb: inMem, db: db, logger: logger}

	// Case: Low signal
	status := mqttTypes.DeviceStatus{
		SignalStrength: 5, // < 10
		Timestamp:      time.Now().Unix(),
	}

	err := client.checkSignalStrength(context.Background(), "sensor1", status)
	if err != nil {
		t.Errorf("Unexpected error: %v", err)
	}
}

func TestCheckErrors(t *testing.T) {
	logger := zerolog.Nop()
	inMem := &MockInMemoryDB{}
	db := &MockDB{GetEmailHiveBySensorIDResultEmail: "test@test.com", GetEmailHiveBySensorIDResultHive: "Hive1"}
	client := &Client{inMemDb: inMem, db: db, logger: logger}

	// Case: Device errors
	status := mqttTypes.DeviceStatus{
		Errors:    []string{"Sensor fail"},
		Timestamp: time.Now().Unix(),
	}

	err := client.checkErrors(context.Background(), "sensor1", status)
	if err != nil {
		t.Errorf("Unexpected error: %v", err)
	}
}

func TestSendConfig(t *testing.T) {
	logger := zerolog.Nop()
	client := &Client{logger: logger, client: &MockMqttClient{}}

	config := mqttTypes.DeviceConfig{
		SamplingTemp: 60,
	}

	err := client.SendConfig("sensor1", config)
	if err != nil {
		t.Errorf("SendConfig failed: %v", err)
	}
}

func TestAddNoiseAndTemp(t *testing.T) {
	logger := zerolog.Nop()
	db := &MockDB{}
	client := &Client{db: db, logger: logger}

	ctx := context.Background()

	// Noise -1 (ignored)
	err := client.addNoise(ctx, "test@test.com", "Hive1", mqttTypes.DeviceData{Noise: -1})
	if err != nil {
		t.Error(err)
	}

	// Valid Noise
	err = client.addNoise(ctx, "test@test.com", "Hive1", mqttTypes.DeviceData{Noise: 50, NoiseTime: 1234567890})
	if err != nil {
		t.Error(err)
	}

	// Temp -1 (ignored)
	err = client.addTemperature(ctx, "test@test.com", "Hive1", mqttTypes.DeviceData{Temperature: -1})
	if err != nil {
		t.Error(err)
	}

	// Valid Temp
	err = client.addTemperature(ctx, "test@test.com", "Hive1", mqttTypes.DeviceData{Temperature: 25, TemperatureTime: 1234567890})
	if err != nil {
		t.Error(err)
	}
}

func TestCheckBatteryLevel_DBError(t *testing.T) {
	logger := zerolog.Nop()
	db := &MockDB{GetEmailHiveBySensorIDError: context.DeadlineExceeded}
	client := &Client{db: db, logger: logger, inMemDb: &MockInMemoryDB{}}

	status := mqttTypes.DeviceStatus{BatteryLevel: 10} // Low to trigger DB call
	err := client.checkBatteryLevel(context.Background(), "s1", status)
	if err == nil {
		t.Error("Expected error from checkBatteryLevel when DB fails")
	}
}

func TestCheckSignalStrength_DBError(t *testing.T) {
	logger := zerolog.Nop()
	db := &MockDB{GetEmailHiveBySensorIDError: context.DeadlineExceeded}
	client := &Client{db: db, logger: logger, inMemDb: &MockInMemoryDB{}}

	status := mqttTypes.DeviceStatus{SignalStrength: 5} // Low to trigger DB call
	err := client.checkSignalStrength(context.Background(), "s1", status)
	if err == nil {
		t.Error("Expected error from checkSignalStrength when DB fails")
	}
}

func TestCheckErrors_DBError(t *testing.T) {
	logger := zerolog.Nop()
	db := &MockDB{GetEmailHiveBySensorIDError: context.DeadlineExceeded}
	client := &Client{db: db, logger: logger, inMemDb: &MockInMemoryDB{}}

	status := mqttTypes.DeviceStatus{Errors: []string{"error"}} // Present to trigger DB call
	err := client.checkErrors(context.Background(), "s1", status)
	if err == nil {
		t.Error("Expected error from checkErrors when DB fails")
	}
}

func TestHandlingStatusData_UpdateTimestampError(t *testing.T) {
	logger := zerolog.Nop()
	inMem := &MockInMemoryDB{
		ExistSensorResult:    true,
		UpdateTimestampError: context.DeadlineExceeded,
	}
	client := &Client{logger: logger, inMemDb: inMem}

	client.handlingStatusData(mqttTypes.DeviceStatus{}, "s1")
	// Should log error `Failed to update timestamp` and return
}

func TestHandlingStatusData_CheckExistError(t *testing.T) {
	logger := zerolog.Nop()
	inMem := &MockInMemoryDB{
		ExistSensorError: context.DeadlineExceeded,
	}
	client := &Client{logger: logger, inMemDb: inMem}

	client.handlingStatusData(mqttTypes.DeviceStatus{}, "s1")
	// Should log error `Failed to check existence of sensor` and return
}

func TestHandleDeviceDataTest_InvalidTopicAndBadPayload(t *testing.T) {
	logger := zerolog.Nop()
	client := &Client{logger: logger}

	// invalid topic
	msg := &MockMessage{topic: "/wrong/topic/format", payload: []byte("{}")}
	client.handleDeviceData(nil, msg)

	// bad payload (invalid json)
	msg2 := &MockMessage{topic: "/device/s1/data", payload: []byte("not json")}
	client.handleDeviceData(nil, msg2)
}

func TestHandleDeviceStatusTest_InvalidTopicAndBadPayload(t *testing.T) {
	logger := zerolog.Nop()
	client := &Client{logger: logger}

	// invalid topic
	msg := &MockMessage{topic: "/device//bad", payload: []byte("{}")}
	client.handleDeviceStatus(nil, msg)

	// bad payload
	msg2 := &MockMessage{topic: "/device/s1/status", payload: []byte("not json")}
	client.handleDeviceStatus(nil, msg2)
}

func TestPublishJSON_MarshalError(t *testing.T) {
	logger := zerolog.Nop()
	client := &Client{logger: logger, client: &MockMqttClient{}}

	// channels cannot be marshaled to JSON -> should return marshal error
	err := client.publishJSON(client.client, "/topic", 1, false, make(chan int))
	if err == nil || !strings.Contains(err.Error(), "marshal payload") {
		t.Fatalf("expected marshal error, got %v", err)
	}
}

func TestPublishJSON_TimeoutAndError(t *testing.T) {
	logger := zerolog.Nop()
	// timeout case
	to1 := &GenericMockToken{waitRet: true, waitTimeoutRet: false, err: nil}
	mc1 := &MockMqttClientWithToken{token: to1}
	client1 := &Client{logger: logger, client: mc1}
	err := client1.publishJSON(mc1, "/t", 1, false, map[string]string{"a": "b"})
	if err == nil || !strings.Contains(err.Error(), "publish timeout") {
		t.Fatalf("expected publish timeout, got %v", err)
	}

	// publish error case
	errToken := &GenericMockToken{waitRet: true, waitTimeoutRet: true, err: fmt.Errorf("pub failed")}
	mc2 := &MockMqttClientWithToken{token: errToken}
	client2 := &Client{logger: logger, client: mc2}
	err = client2.publishJSON(mc2, "/t", 1, false, map[string]string{"a": "b"})
	if err == nil || !strings.Contains(err.Error(), "publish error") {
		t.Fatalf("expected publish error, got %v", err)
	}
}

func TestHandlingStatusData_SensorNotExist_SetSensorCalled(t *testing.T) {
	logger := zerolog.Nop()
	inMem := &MockInMemoryDB{ExistSensorResult: false}
	db := &MockDB{GetEmailHiveBySensorIDResultEmail: "e@e", GetEmailHiveBySensorIDResultHive: "H"}
	client := &Client{logger: logger, inMemDb: inMem, db: db, client: &MockMqttClient{}}

	client.handlingStatusData(mqttTypes.DeviceStatus{Timestamp: time.Now().Unix(), BatteryLevel: 100, SignalStrength: 100}, "s-new")

	if !inMem.SetSensorCall {
		t.Fatalf("expected SetSensor to be called when sensor did not exist")
	}
}

func TestCheckBatterySignal_NoNotifications(t *testing.T) {
	logger := zerolog.Nop()
	inMem := &MockInMemoryDB{}
	d := &MockDB{GetEmailHiveBySensorIDResultEmail: "e@e", GetEmailHiveBySensorIDResultHive: "H"}
	client := &Client{logger: logger, inMemDb: inMem, db: d}

	// battery ok
	err := client.checkBatteryLevel(context.Background(), "s1", mqttTypes.DeviceStatus{BatteryLevel: 50, Timestamp: time.Now().Unix()})
	if err != nil {
		t.Fatalf("expected no error for sufficient battery, got %v", err)
	}

	// signal ok
	err = client.checkSignalStrength(context.Background(), "s1", mqttTypes.DeviceStatus{SignalStrength: 50, Timestamp: time.Now().Unix()})
	if err != nil {
		t.Fatalf("expected no error for sufficient signal, got %v", err)
	}
}

func TestCheckErrors_NoErrors(t *testing.T) {
	logger := zerolog.Nop()
	inMem := &MockInMemoryDB{}
	d := &MockDB{GetEmailHiveBySensorIDResultEmail: "e@e", GetEmailHiveBySensorIDResultHive: "H"}
	client := &Client{logger: logger, inMemDb: inMem, db: d}

	err := client.checkErrors(context.Background(), "s1", mqttTypes.DeviceStatus{Errors: []string{}, Timestamp: time.Now().Unix()})
	if err != nil {
		t.Fatalf("expected nil when no errors present, got %v", err)
	}
}
