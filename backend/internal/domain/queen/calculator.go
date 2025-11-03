package queen

import (
	"BeeIOT/internal/domain/types/httpType"
	"time"
)

func CalculatePreciseCalendar(start time.Time) httpType.QueenPhaseCalendar {
	layout := "2006-01-02"

	q := httpType.QueenPhaseCalendar{}
	q.StartDate = start.Format(layout)

	//egg
	q.EggPhase.Standing = start.Format(layout)
	q.EggPhase.Tilted = start.AddDate(0, 0, 1).Format(layout)
	q.EggPhase.Lying = start.AddDate(0, 0, 2).Format(layout)

	//larva
	q.LarvaPhase.Start = start.AddDate(0, 0, 3).Format(layout)
	q.LarvaPhase.Day1 = start.AddDate(0, 0, 4).Format(layout)
	q.LarvaPhase.Day2 = start.AddDate(0, 0, 5).Format(layout)
	q.LarvaPhase.Day3 = start.AddDate(0, 0, 6).Format(layout)
	q.LarvaPhase.Day4 = start.AddDate(0, 0, 7).Format(layout)
	q.LarvaPhase.Day5 = start.AddDate(0, 0, 8).Format(layout)
	q.LarvaPhase.Sealed = start.AddDate(0, 0, 8).Format(layout)

	//pupa (sealed)
	q.PupaPhase.Start = start.AddDate(0, 0, 8).Format(layout)
	q.PupaPhase.End = start.AddDate(0, 0, 12).Format(layout)
	q.PupaPhase.Duration = "4 дня" // опциональное поле, мб для UI понадобится
	//pupa (selection)
	q.PupaPhase.Selection = start.AddDate(0, 0, 13).Format(layout)

	//emergence queen
	q.QueenPhase.EmergenceStart = start.AddDate(0, 0, 14).Format(layout)
	q.QueenPhase.EmergenceEnd = start.AddDate(0, 0, 15).Format(layout)

	// maturation
	q.QueenPhase.MaturationStart = start.AddDate(0, 0, 16).Format(layout)
	q.QueenPhase.MaturationEnd = start.AddDate(0, 0, 20).Format(layout)

	//mating flight
	q.QueenPhase.MatingFlightStart = start.AddDate(0, 0, 21).Format(layout)
	q.QueenPhase.MatingFlightEnd = start.AddDate(0, 0, 23).Format(layout)

	//insemination
	q.QueenPhase.InseminationStart = start.AddDate(0, 0, 24).Format(layout)
	q.QueenPhase.InseminationEnd = start.AddDate(0, 0, 26).Format(layout)

	//egg laying check
	q.QueenPhase.EggLayingCheckStart = start.AddDate(0, 0, 27).Format(layout)
	q.QueenPhase.EggLayingCheckEnd = start.AddDate(0, 0, 29).Format(layout)

	return q
}

func ParseDate(dateStr string) (time.Time, error) {
	return time.Parse("2006-01-02", dateStr)
}
