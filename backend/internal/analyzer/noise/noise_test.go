package noise

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
	NoiseData map[time.Time][]dbTypes.HivesNoiseData
	MockEmail string
}

func (m *MockDB) GetHives(_ context.Context, _ string) ([]dbTypes.Hive, error) {
	return m.Hives, nil
}

func (m *MockDB) GetNoiseSinceDay(_ context.Context, _ int, _ time.Time) (map[time.Time][]dbTypes.HivesNoiseData, error) {
	return m.NoiseData, nil
}

func (m *MockDB) GetUserById(_ context.Context, _ int) (string, error) {
	return m.MockEmail, nil
}

type MockInMemoryDB struct {
	interfaces.InMemoryDB
	Notifications []httpType.NotificationData
}

func (m *MockInMemoryDB) SetNotification(_ context.Context, _ string, note httpType.NotificationData) error {
	m.Notifications = append(m.Notifications, note)
	return nil
}

func TestAnalyzeNoise(t *testing.T) {
	// Setup context
	ctx := context.TODO()

	// Setup MockDB
	noiseData := make(map[time.Time][]dbTypes.HivesNoiseData)

	// We need dates that match the logic in analyzeDay.
	// logic:
	// for date, noises := range data {
	//    if date == curTime { continue }
	//    prevTime := ...
	//    if prevData, ok := data[prevTime.Add(-24*time.Hour)]; ok { ...
	//
	// Wait, the logic in implementation seems to be looking for `prevTime.Add(-24*time.Hour)`.
	// Let's look at `noise.go` again carefully.
	// prevTime := time.Date(date.Year(), date.Month(), date.Day(), 0, 0, 0, 0, time.UTC)
	// if prevData, ok := data[prevTime.Add(-24*time.Hour)]; ok {
	//
	// So if `date` is Today(00:00), `prevTime` is Today(00:00).
	// It looks for data at Today - 24h = Yesterday.
	// And compares `noises` (Today's data?) with `prevData` (Yesterday's data).
	// The variable naming `prevTime` is slightly confusing or my reading is.
	//
	// Let's set up:
	// "Reference Date" (the date being iterated) -> High Noise
	// "Reference Date - 1 Day" -> Low Noise
	//
	// Then `prevTime.Add(-24*h)` will successfully find the Low Noise data.
	// And `noises` will be the High Noise data.
	// And the diff > 200.

	ct := time.Now()
	// Normalize to start of day as the analyzer does
	todayStart := time.Date(ct.Year(), ct.Month(), ct.Day(), 0, 0, 0, 0, time.UTC)

	// The loop iterates over `data` keys.
	// If we put data for `todayStart`, it skips it (`if date == curTime`).

	// So we need data for `yesterdayStart`.
	yesterdayStart := todayStart.Add(-24 * time.Hour)

	// And we need data for `dayBeforeYesterday`.
	dayBeforeYesterdayStart := yesterdayStart.Add(-24 * time.Hour)

	// "noises" (current iteration) will be at yesterdayStart
	// "prevData" will be looked up at yesterdayStart - 24h = dayBeforeYesterdayStart.

	noiseData[dayBeforeYesterdayStart] = []dbTypes.HivesNoiseData{
		{Level: 100}, {Level: 120}, {Level: 110}, // Low noise
	}

	noiseData[yesterdayStart] = []dbTypes.HivesNoiseData{
		{Level: 500}, {Level: 550}, {Level: 525}, // High noise
	}

	mockDB := &MockDB{
		Hives: []dbTypes.Hive{
			{Id: 1, NameHive: "NoiseHive"},
		},
		NoiseData: noiseData,
		MockEmail: "noise@example.com",
	}

	mockInMemDB := &MockInMemoryDB{}

	// NewAnalyzer expects logger in context or creates one.
	logger := zerolog.Nop()
	ctxLogger := context.WithValue(ctx, "logger", logger)

	analyzer := NewAnalyzer(ctxLogger, 1*time.Hour, mockDB, mockInMemDB)

	// Run analysis
	analyzer.analyzeNoise()

	// Check results
	if len(mockInMemDB.Notifications) != 1 {
		t.Errorf("Expected 1 notification, got %d", len(mockInMemDB.Notifications))
	} else {
		note := mockInMemDB.Notifications[0]
		if note.NameHive != "NoiseHive" {
			t.Errorf("Expected hive name NoiseHive, got %s", note.NameHive)
		}
	}
}

func TestAverageNoise(t *testing.T) {
	analyzer := &Analyzer{} // methods are on pointer receiver

	data := []dbTypes.HivesNoiseData{
		{Level: 100},
		{Level: 200},
		{Level: 300},
	}

	avg := analyzer.averageNoise(data)
	if avg != 200.0 {
		t.Errorf("Expected average 200.0, got %f", avg)
	}

	emptyData := []dbTypes.HivesNoiseData{}
	avgEmpty := analyzer.averageNoise(emptyData)
	if avgEmpty != 0.0 {

	}
}
