package temperature

import (
	"BeeIOT/internal/domain/interfaces"
	"BeeIOT/internal/domain/models/dbTypes"
	"BeeIOT/internal/domain/models/httpType"
	"context"
	"testing"
	"time"

	"github.com/rs/zerolog"
)

type MockDB struct {
	interfaces.DB
	Hives     []dbTypes.Hive
	TempData  []dbTypes.HivesTemperatureData
	MockEmail string
}

func (m *MockDB) GetHives(ctx context.Context, email string) ([]dbTypes.Hive, error) {
	return m.Hives, nil
}

func (m *MockDB) GetTemperaturesSinceTimeById(ctx context.Context, hiveId int, t time.Time) ([]dbTypes.HivesTemperatureData, error) {
	return m.TempData, nil
}

func (m *MockDB) UpdateHiveTemperatureCheck(ctx context.Context, hiveId int, t time.Time) error {
	return nil
}

func (m *MockDB) GetUserById(ctx context.Context, id int) (string, error) {
	return m.MockEmail, nil
}

type MockInMemoryDB struct {
	interfaces.InMemoryDB
	Notifications []httpType.NotificationData
}

func (m *MockInMemoryDB) SetNotification(ctx context.Context, email string, note httpType.NotificationData) error {
	m.Notifications = append(m.Notifications, note)
	return nil
}

func TestAnalyzeTemperature(t *testing.T) {
	ctx := context.WithValue(context.Background(), "logger", zerolog.Nop())

	mockDB := &MockDB{
		Hives: []dbTypes.Hive{
			{Id: 1, NameHive: "Hive1", DateTemperature: time.Now().Add(-1 * time.Hour)},
		},
		TempData: []dbTypes.HivesTemperatureData{
			{Temperature: 45.0, Date: time.Now()}, // Too high
			{Temperature: 34.0, Date: time.Now()}, // Normal
			{Temperature: 20.0, Date: time.Now()}, // Too low
		},
		MockEmail: "test@example.com",
	}

	mockInMemDB := &MockInMemoryDB{}

	analyzer := NewAnalyzer(ctx, 1*time.Second, mockDB, mockInMemDB)

	analyzer.analyzeTemperature()

	if len(mockInMemDB.Notifications) != 2 {
		t.Errorf("Expected 2 notifications, got %d", len(mockInMemDB.Notifications))
	}
}

func TestIsNormallyTemperature(t *testing.T) {
	ctx := context.WithValue(context.Background(), "logger", zerolog.Nop())
	analyzer := NewAnalyzer(ctx, 1*time.Second, nil, nil)

	tests := []struct {
		temp     float64
		expected bool
	}{
		{34.0, true},
		{39.0, true},
		{29.0, true},
		{39.1, false},
		{28.9, false},
		{100.0, false},
		{0.0, false},
	}

	for _, test := range tests {
		result := analyzer.isNormallyTemperature(test.temp)
		if result != test.expected {
			t.Errorf("For temp %.2f expected %v, got %v", test.temp, test.expected, result)
		}
	}
}
