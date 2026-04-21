package handlers

import (
	"BeeIOT/internal/domain/models/dbTypes"
	"BeeIOT/internal/domain/models/httpType"
	"errors"
	"net/http"
	"strconv"

	"github.com/go-chi/chi/v5"
	"github.com/jackc/pgx/v5"
)

func (h *Handler) CreateInstruction(w http.ResponseWriter, r *http.Request) {
	var req httpType.CreateInstructionRequest
	if err := h.readBodyJSON(w, r, &req); err != nil {
		return
	}

	if req.Title == "" {
		http.Error(w, "Заголовок инструкции обязателен", http.StatusBadRequest)
		return
	}
	if req.Content == "" {
		http.Error(w, "Содержимое инструкции обязательно", http.StatusBadRequest)
		return
	}

	id, err := h.db.CreateInstruction(r.Context(), req)
	if err != nil {
		h.logger.Error().Err(err).Msg("failed to create instruction")
		http.Error(w, "Внутренняя ошибка сервера", http.StatusInternalServerError)
		return
	}

	h.writeBodyJSON(w, "Инструкция успешно создана", map[string]int{"id": id})
}

func (h *Handler) GetInstructions(w http.ResponseWriter, r *http.Request) {
	items, err := h.db.GetInstructions(r.Context())
	if err != nil {
		h.logger.Error().Err(err).Msg("failed to get instructions")
		http.Error(w, "Внутренняя ошибка сервера", http.StatusInternalServerError)
		return
	}

	result := make([]httpType.InstructionItem, len(items))
	for i, it := range items {
		result[i] = dbInstructionToItem(it)
	}
	h.writeBodyJSON(w, "Список инструкций получен", result)
}

func (h *Handler) DeleteInstruction(w http.ResponseWriter, r *http.Request) {
	idStr := chi.URLParam(r, "id")
	id, err := strconv.Atoi(idStr)
	if err != nil || id <= 0 {
		http.Error(w, "Неверный ID инструкции", http.StatusBadRequest)
		return
	}

	if err := h.db.DeleteInstruction(r.Context(), id); err != nil {
		if errors.Is(err, pgx.ErrNoRows) {
			http.Error(w, "Инструкция не найдена", http.StatusNotFound)
			return
		}
		h.logger.Error().Err(err).Int("id", id).Msg("failed to delete instruction")
		http.Error(w, "Внутренняя ошибка сервера", http.StatusInternalServerError)
		return
	}

	h.writeBodyJSON(w, "Инструкция успешно удалена", nil)
}

func dbInstructionToItem(i dbTypes.Instruction) httpType.InstructionItem {
	return httpType.InstructionItem{
		ID:        i.ID,
		Title:     i.Title,
		Content:   i.Content,
		CreatedAt: i.CreatedAt.Unix(),
	}
}
