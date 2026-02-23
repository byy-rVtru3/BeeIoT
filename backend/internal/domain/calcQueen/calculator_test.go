package calcQueen

import (
	"testing"
	"time"
)

func TestParseDate(t *testing.T) {
	tests := []struct {
		name    string
		input   string
		want    time.Time
		wantErr bool
	}{
		{
			name:  "Valid date",
			input: "2023-05-01",
			want:  time.Date(2023, 5, 1, 0, 0, 0, 0, time.UTC),
		},
		{
			name:    "Invalid format",
			input:   "01-05-2023",
			wantErr: true,
		},
		{
			name:    "Invalid date",
			input:   "2023-02-30",
			wantErr: true,
		},
	}

	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			got, err := ParseDate(tt.input)
			if (err != nil) != tt.wantErr {
				t.Errorf("ParseDate() error = %v, wantErr %v", err, tt.wantErr)
				return
			}
			if !tt.wantErr && !got.Equal(tt.want) {
				t.Errorf("ParseDate() = %v, want %v", got, tt.want)
			}
		})
	}
}

func TestCalculatePreciseCalendar(t *testing.T) {
	start := time.Date(2023, 5, 1, 0, 0, 0, 0, time.UTC)
	q := &QueenPhaseCalendar{}
	q.CalculatePreciseCalendar(start)

	tests := []struct {
		name     string
		got      string
		expected string
	}{
		{"StartDate", q.StartDate, "2023-05-01"},
		{"EggPhase.Standing", q.EggPhase.Standing, "2023-05-01"},
		{"EggPhase.Tilted", q.EggPhase.Tilted, "2023-05-02"},                               // +1 day
		{"EggPhase.Lying", q.EggPhase.Lying, "2023-05-03"},                                 // +2 days
		{"LarvaPhase.Start", q.LarvaPhase.Start, "2023-05-04"},                             // +3 days
		{"LarvaPhase.Sealed", q.LarvaPhase.Sealed, "2023-05-09"},                           // +8 days
		{"PupaPhase.Start", q.PupaPhase.Start, "2023-05-09"},                               // +8 days
		{"PupaPhase.End", q.PupaPhase.End, "2023-05-13"},                                   // +12 days
		{"QueenPhase.EmergenceStart", q.QueenPhase.EmergenceStart, "2023-05-15"},           // +14 days
		{"QueenPhase.MatingFlightStart", q.QueenPhase.MatingFlightStart, "2023-05-22"},     // +21 days
		{"QueenPhase.EggLayingCheckStart", q.QueenPhase.EggLayingCheckStart, "2023-05-28"}, // +27 days
	}

	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			if tt.got != tt.expected {
				t.Errorf("%s = %v, want %v", tt.name, tt.got, tt.expected)
			}
		})
	}
}
