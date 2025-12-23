package calcQueen

import (
	"testing"
	"time"

	"github.com/stretchr/testify/assert"
)

func TestQueenPhaseCalendar_CalculatePreciseCalendar(t *testing.T) {
	calendar := &QueenPhaseCalendar{}
	startDate := time.Date(2025, 5, 1, 0, 0, 0, 0, time.UTC)

	t.Run("Calculate full queen development calendar", func(t *testing.T) {
		calendar.CalculatePreciseCalendar(startDate)

		// Проверяем дату начала
		assert.Equal(t, "2025-05-01", calendar.StartDate)

		// Проверяем фазу яйца
		assert.Equal(t, "2025-05-01", calendar.EggPhase.Standing)
		assert.Equal(t, "2025-05-02", calendar.EggPhase.Tilted)
		assert.Equal(t, "2025-05-03", calendar.EggPhase.Lying)

		// Проверяем фазу личинки
		assert.Equal(t, "2025-05-04", calendar.LarvaPhase.Start)
		assert.Equal(t, "2025-05-05", calendar.LarvaPhase.Day1)
		assert.Equal(t, "2025-05-06", calendar.LarvaPhase.Day2)
		assert.Equal(t, "2025-05-07", calendar.LarvaPhase.Day3)
		assert.Equal(t, "2025-05-08", calendar.LarvaPhase.Day4)
		assert.Equal(t, "2025-05-09", calendar.LarvaPhase.Day5)
		assert.Equal(t, "2025-05-09", calendar.LarvaPhase.Sealed)

		// Проверяем фазу куколки
		assert.Equal(t, "2025-05-09", calendar.PupaPhase.Start)
		assert.Equal(t, "2025-05-13", calendar.PupaPhase.End)
		assert.Equal(t, "4 дня", calendar.PupaPhase.Duration)
		assert.Equal(t, "2025-05-14", calendar.PupaPhase.Selection)

		// Проверяем фазу матки
		assert.Equal(t, "2025-05-15", calendar.QueenPhase.EmergenceStart)
		assert.Equal(t, "2025-05-16", calendar.QueenPhase.EmergenceEnd)
		assert.Equal(t, "2025-05-17", calendar.QueenPhase.MaturationStart)
		assert.Equal(t, "2025-05-21", calendar.QueenPhase.MaturationEnd)
		assert.Equal(t, "2025-05-22", calendar.QueenPhase.MatingFlightStart)
		assert.Equal(t, "2025-05-24", calendar.QueenPhase.MatingFlightEnd)
		assert.Equal(t, "2025-05-25", calendar.QueenPhase.InseminationStart)
		assert.Equal(t, "2025-05-27", calendar.QueenPhase.InseminationEnd)
		assert.Equal(t, "2025-05-28", calendar.QueenPhase.EggLayingCheckStart)
		assert.Equal(t, "2025-05-30", calendar.QueenPhase.EggLayingCheckEnd)
	})

	t.Run("Calendar with different start date", func(t *testing.T) {
		winterStart := time.Date(2025, 12, 1, 0, 0, 0, 0, time.UTC)
		winterCalendar := &QueenPhaseCalendar{}
		winterCalendar.CalculatePreciseCalendar(winterStart)

		assert.Equal(t, "2025-12-01", winterCalendar.StartDate)
		assert.Equal(t, "2025-12-01", winterCalendar.EggPhase.Standing)
		assert.Equal(t, "2025-12-15", winterCalendar.QueenPhase.EmergenceStart)
		assert.Equal(t, "2025-12-30", winterCalendar.QueenPhase.EggLayingCheckEnd)
	})

	t.Run("Calendar crossing year boundary", func(t *testing.T) {
		yearEndStart := time.Date(2025, 12, 20, 0, 0, 0, 0, time.UTC)
		yearEndCalendar := &QueenPhaseCalendar{}
		yearEndCalendar.CalculatePreciseCalendar(yearEndStart)

		assert.Equal(t, "2025-12-20", yearEndCalendar.StartDate)
		assert.Equal(t, "2026-01-03", yearEndCalendar.QueenPhase.EmergenceStart)
		assert.Equal(t, "2026-01-18", yearEndCalendar.QueenPhase.EggLayingCheckEnd)
	})
}

func TestParseDate(t *testing.T) {
	tests := []struct {
		name        string
		dateStr     string
		expectError bool
		expected    time.Time
	}{
		{
			name:        "Valid date format",
			dateStr:     "2025-05-01",
			expectError: false,
			expected:    time.Date(2025, 5, 1, 0, 0, 0, 0, time.UTC),
		},
		{
			name:        "Valid winter date",
			dateStr:     "2025-12-25",
			expectError: false,
			expected:    time.Date(2025, 12, 25, 0, 0, 0, 0, time.UTC),
		},
		{
			name:        "Invalid date format - wrong separator",
			dateStr:     "2025/05/01",
			expectError: true,
		},
		{
			name:        "Invalid date format - missing day",
			dateStr:     "2025-05",
			expectError: true,
		},
		{
			name:        "Invalid date - non-existent date",
			dateStr:     "2025-02-30",
			expectError: true,
		},
		{
			name:        "Invalid date - wrong format",
			dateStr:     "01-05-2025",
			expectError: true,
		},
		{
			name:        "Empty string",
			dateStr:     "",
			expectError: true,
		},
		{
			name:        "Invalid characters",
			dateStr:     "abc-def-ghi",
			expectError: true,
		},
	}

	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			result, err := ParseDate(tt.dateStr)

			if tt.expectError {
				assert.Error(t, err)
			} else {
				assert.NoError(t, err)
				assert.Equal(t, tt.expected, result)
			}
		})
	}
}

func TestQueenPhaseCalendar_SequentialDates(t *testing.T) {
	calendar := &QueenPhaseCalendar{}
	startDate := time.Date(2025, 6, 10, 0, 0, 0, 0, time.UTC)
	calendar.CalculatePreciseCalendar(startDate)

	t.Run("Egg phase progression", func(t *testing.T) {
		standing, _ := ParseDate(calendar.EggPhase.Standing)
		tilted, _ := ParseDate(calendar.EggPhase.Tilted)
		lying, _ := ParseDate(calendar.EggPhase.Lying)

		assert.Equal(t, 1, int(tilted.Sub(standing).Hours()/24))
		assert.Equal(t, 1, int(lying.Sub(tilted).Hours()/24))
	})

	t.Run("Larva phase progression", func(t *testing.T) {
		start, _ := ParseDate(calendar.LarvaPhase.Start)
		day1, _ := ParseDate(calendar.LarvaPhase.Day1)
		sealed, _ := ParseDate(calendar.LarvaPhase.Sealed)

		assert.Equal(t, 1, int(day1.Sub(start).Hours()/24))
		assert.Equal(t, 5, int(sealed.Sub(start).Hours()/24))
	})

	t.Run("Complete development cycle timing", func(t *testing.T) {
		eggStart, _ := ParseDate(calendar.EggPhase.Standing)
		queenEmerge, _ := ParseDate(calendar.QueenPhase.EmergenceStart)
		finalCheck, _ := ParseDate(calendar.QueenPhase.EggLayingCheckEnd)

		// От начала до выхода матки - 14 дней
		assert.Equal(t, 14, int(queenEmerge.Sub(eggStart).Hours()/24))

		// Полный цикл до проверки засева - 29 дней
		assert.Equal(t, 29, int(finalCheck.Sub(eggStart).Hours()/24))
	})
}

func TestQueenPhaseCalendar_PhaseDurations(t *testing.T) {
	calendar := &QueenPhaseCalendar{}
	startDate := time.Date(2025, 7, 15, 0, 0, 0, 0, time.UTC)
	calendar.CalculatePreciseCalendar(startDate)

	t.Run("Egg phase duration", func(t *testing.T) {
		start, _ := ParseDate(calendar.EggPhase.Standing)
		end, _ := ParseDate(calendar.LarvaPhase.Start)

		duration := int(end.Sub(start).Hours() / 24)
		assert.Equal(t, 3, duration, "Egg phase should last 3 days")
	})

	t.Run("Larva phase duration", func(t *testing.T) {
		start, _ := ParseDate(calendar.LarvaPhase.Start)
		end, _ := ParseDate(calendar.PupaPhase.Start)

		duration := int(end.Sub(start).Hours() / 24)
		assert.Equal(t, 5, duration, "Larva phase should last 5 days")
	})

	t.Run("Maturation phase duration", func(t *testing.T) {
		start, _ := ParseDate(calendar.QueenPhase.MaturationStart)
		end, _ := ParseDate(calendar.QueenPhase.MaturationEnd)

		duration := int(end.Sub(start).Hours() / 24)
		assert.Equal(t, 4, duration, "Maturation phase should last 4 days")
	})

	t.Run("Mating flight window", func(t *testing.T) {
		start, _ := ParseDate(calendar.QueenPhase.MatingFlightStart)
		end, _ := ParseDate(calendar.QueenPhase.MatingFlightEnd)

		duration := int(end.Sub(start).Hours() / 24)
		assert.Equal(t, 2, duration, "Mating flight window should be 2 days")
	})
}
