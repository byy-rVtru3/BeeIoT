package handlers

import (
	"BeeIOT/internal/domain/queen"
	"BeeIOT/internal/domain/types/httpType"
	"encoding/json"
	"log/slog"
	"net/http"
)

func (h *Handler) QueenCalculator(w http.ResponseWriter, r *http.Request) {
	var req httpType.QueenRequest

	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		slog.Error("Failed to decode JSON request",
			"module", "handlers",
			"function", "QueenCalculator",
			"error", err)
		http.Error(w, "Неверный формат JSON", http.StatusBadRequest)
		return
	}

	start, err := queen.ParseDate(req.StartDate)
	if err != nil {
		slog.Warn("Invalid date format",
			"module", "handlers",
			"function", "QueenCalculator",
			"error", err)
		http.Error(w, "Неверный формат даты. Пример: 2025-05-15", http.StatusBadRequest)
		return
	}

	result := queen.CalculatePreciseCalendar(start)

	resp := Response{
		Status:  "ok",
		Message: "Календарь развития матки успешно рассчитан",
		Data:    result,
	}

	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(http.StatusOK)

	if err := json.NewEncoder(w).Encode(resp); err != nil {
		slog.Error("Failed to encode JSON response",
			"module", "handlers",
			"function", "QueenCalculator",
			"error", err)
	}
}
