package handlers

import (
	"BeeIOT/internal/domain/calcQueen"
	"BeeIOT/internal/domain/models/httpType"
	"net/http"
)

func (h *Handler) QueenCalculator(w http.ResponseWriter, r *http.Request) {
	var req httpType.QueenRequest
	if err := h.readBodyJSON(w, r, &req); err != nil {
		return
	}

	start, err := calcQueen.ParseDate(req.StartDate)
	if err != nil {
		h.logger.Warn().Err(err).Msg("invalid date format")
		http.Error(w, "Неверный формат даты", http.StatusBadRequest)
		return
	}

	calendar := &calcQueen.QueenPhaseCalendar{}
	calendar.CalculatePreciseCalendar(start)

	h.writeBodyJSON(w, "Календарь развития матки успешно рассчитан", calendar)
}
