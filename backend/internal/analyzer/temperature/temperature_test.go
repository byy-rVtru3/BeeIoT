package temperature

import (
	"BeeIOT/internal/domain/interfaces"
	"BeeIOT/internal/domain/models/dbTypes"
	"context"
	"testing"
	"time"

	"github.com/rs/zerolog"
)

type MockDB struct {
	interfaces.DB
	Hives    []dbTypes.Hive
	TempData []dbTypes.HivesTemperatureData
}

func (m *MockDB) GetHives(ctx context.Context, email string, active *bool) ([]dbTypes.Hive, error) {
	return m.Hives, nil
}

func (m *MockDB) GetTemperaturesSinceTimeById(ctx context.Context, hiveId int, t time.Time) ([]dbTypes.HivesTemperatureData, error) {
	return m.TempData, nil
}

func (m *MockDB) UpdateHiveTemperatureCheck(ctx context.Context, hiveId int, t time.Time) error {
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
	}

	analyzer := NewAnalyzer(ctx, 1*time.Second, mockDB, nil)

	// With nil notification, temperatureAnalysis skips notification sending
	analyzer.analyzeTemperature()
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
