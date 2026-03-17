package handlers

import (
	"BeeIOT/internal/domain/models/httpType"
	"BeeIOT/internal/domain/calcQueen"
	"net/http"
	"time"
)

func (h *Handler) CreateQueen(w http.ResponseWriter, r *http.Request) {
	email, err := h.getEmailFromContext(w, r)
	if err != nil {
		return
	}

	var req httpType.CreateQueen
	if err := h.readBodyJSON(w, r, &req); err != nil {
		return
	}

	if req.Name == "" || req.StartDate == "" {
		h.logger.Warn().Str("email", email).Msg("queen details are incomplete")
		http.Error(w, "Имя и дата начала обязательны", http.StatusBadRequest)
		return
	}

	startDate, err := time.Parse("2006-01-02", req.StartDate)
	if err != nil {
		h.logger.Warn().Str("email", email).Str("date", req.StartDate).Msg("invalid date format")
		http.Error(w, "Неверный формат даты, ожидается YYYY-MM-DD", http.StatusBadRequest)
		return
	}

	if err := h.db.NewQueen(r.Context(), email, req.Name, req.StartDate); err != nil {
		h.logger.Error().Err(err).Str("email", email).Msg("error creating queen")
		http.Error(w, "Внутренняя ошибка сервера", http.StatusInternalServerError)
		return
	}

	calendar := calcQueen.QueenPhaseCalendar{}
	calendar.CalculatePreciseCalendar(startDate)
	h.logger.Debug().Str("email", email).Str("queen_name", req.Name).Msg("queen created")

	h.writeBodyJSON(w, "Матка создана, календарь рассчитан", calendar)
}

func (h *Handler) GetQueens(w http.ResponseWriter, r *http.Request) {
	email, err := h.getEmailFromContext(w, r)
	if err != nil {
		return
	}

	queens, err := h.db.GetQueens(r.Context(), email)
	if err != nil {
		h.logger.Error().Err(err).Str("email", email).Msg("error getting queens")
		http.Error(w, "Внутренняя ошибка сервера", http.StatusInternalServerError)
		return
	}

	result := make([]httpType.QueenListItem, 0, len(queens))
	for _, qn := range queens {
		result = append(result, httpType.QueenListItem{
			Name:      qn.Name,
			StartDate: qn.StartDate.Format("2006-01-02"),
		})
	}

	h.writeBodyJSON(w, "Список маток успешно получен", result)
}

func (h *Handler) GetQueen(w http.ResponseWriter, r *http.Request) {
	email, err := h.getEmailFromContext(w, r)
	if err != nil {
		return
	}

	queenName := r.URL.Query().Get("name")
	if queenName == "" {
		h.logger.Error().Msg("no \"name\" in request")
		http.Error(w, "Параметр \"name\" обязателен", http.StatusBadRequest)
		return
	}

	queen, err := h.db.GetQueenByName(r.Context(), email, queenName)
	if err != nil {
		h.logger.Error().Err(err).Str("email", email).Str("queen_name", queenName).Msg("error getting queen")
		http.Error(w, "Матка не найдена", http.StatusNotFound)
		return
	}

	calendar := calcQueen.QueenPhaseCalendar{}
	calendar.CalculatePreciseCalendar(queen.StartDate)

	h.writeBodyJSON(w, "Данные о матке получены", httpType.QueenDetails{
		Name:     queen.Name,
		Calendar: calendar,
	})
}

func (h *Handler) UpdateQueen(w http.ResponseWriter, r *http.Request) {
	email, err := h.getEmailFromContext(w, r)
	if err != nil {
		return
	}

	var req httpType.UpdateQueen
	if err := h.readBodyJSON(w, r, &req); err != nil {
		return
	}

	if req.OldName == "" {
		h.logger.Warn().Str("email", email).Msg("old queen name is empty")
		http.Error(w, "Старое имя матки не может быть пустым", http.StatusBadRequest)
		return
	}

	if err := h.db.UpdateQueen(r.Context(), email, req); err != nil {
		h.logger.Error().Err(err).Str("email", email).Str("old_name", req.OldName).Msg("error updating queen")
		http.Error(w, "Внутренняя ошибка сервера", http.StatusInternalServerError)
		return
	}

	h.logger.Debug().Str("email", email).Str("old_name", req.OldName).Msg("queen updated")
	h.writeBodyJSON(w, "Матка успешно обновлена", nil)
}

func (h *Handler) DeleteQueen(w http.ResponseWriter, r *http.Request) {
	email, err := h.getEmailFromContext(w, r)
	if err != nil {
		return
	}

	var req struct {
		Name string `json:"name"`
	}
	if err := h.readBodyJSON(w, r, &req); err != nil {
		return
	}

	if req.Name == "" {
		h.logger.Warn().Str("email", email).Msg("queen name is empty")
		http.Error(w, "Имя матки обязательно", http.StatusBadRequest)
		return
	}

	if err := h.db.DeleteQueen(r.Context(), email, req.Name); err != nil {
		h.logger.Error().Err(err).Str("email", email).Str("queen", req.Name).Msg("error deleting queen")
		http.Error(w, "Внутренняя ошибка сервера", http.StatusInternalServerError)
		return
	}

	h.logger.Debug().Str("email", email).Str("queen", req.Name).Msg("queen deleted")
	h.writeBodyJSON(w, "Матка успешно удалена", nil)
}
