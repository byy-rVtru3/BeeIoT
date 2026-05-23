package handlers

import (
	"BeeIOT/internal/domain/models/dbTypes"
	"BeeIOT/internal/domain/models/httpType"
	"errors"
	"net/http"
	"time"

	"github.com/go-chi/chi/v5"
	"github.com/jackc/pgx/v5"
)

const instructionItemTitleMax = 100

func (h *Handler) GetInstructionItems(w http.ResponseWriter, r *http.Request) {
	items, err := h.db.GetInstructionItems(r.Context())
	if err != nil {
		h.logger.Error().Err(err).Msg("failed to get instruction items")
		http.Error(w, "Внутренняя ошибка сервера", http.StatusInternalServerError)
		return
	}
	h.writeBodyJSON(w, "Список инструкции получен", itemsToHTTP(items))
}

func (h *Handler) CreateInstructionItem(w http.ResponseWriter, r *http.Request) {
	var req httpType.CreateInstructionItemRequest
	if err := h.readBodyJSON(w, r, &req); err != nil {
		return
	}
	if req.Title == "" || len(req.Title) > instructionItemTitleMax {
		http.Error(w, "Заголовок: 1..100 символов", http.StatusBadRequest)
		return
	}
	if req.Body == "" {
		http.Error(w, "Тело пункта обязательно", http.StatusBadRequest)
		return
	}

	it, err := h.db.CreateInstructionItem(r.Context(), req)
	if err != nil {
		h.logger.Error().Err(err).Msg("failed to create instruction item")
		http.Error(w, "Внутренняя ошибка сервера", http.StatusInternalServerError)
		return
	}
	h.writeBodyJSON(w, "Пункт инструкции создан", itemToHTTP(it))
}

func (h *Handler) UpdateInstructionItem(w http.ResponseWriter, r *http.Request) {
	id := chi.URLParam(r, "id")
	if id == "" {
		http.Error(w, "ID пункта обязателен", http.StatusBadRequest)
		return
	}

	var req httpType.UpdateInstructionItemRequest
	if err := h.readBodyJSON(w, r, &req); err != nil {
		return
	}
	if req.Title != nil && (*req.Title == "" || len(*req.Title) > instructionItemTitleMax) {
		http.Error(w, "Заголовок: 1..100 символов", http.StatusBadRequest)
		return
	}
	if req.Body != nil && *req.Body == "" {
		http.Error(w, "Тело пункта не может быть пустым", http.StatusBadRequest)
		return
	}

	it, err := h.db.UpdateInstructionItem(r.Context(), id, req)
	if err != nil {
		if errors.Is(err, pgx.ErrNoRows) {
			http.Error(w, "Пункт не найден", http.StatusNotFound)
			return
		}
		h.logger.Error().Err(err).Str("id", id).Msg("failed to update instruction item")
		http.Error(w, "Внутренняя ошибка сервера", http.StatusInternalServerError)
		return
	}
	h.writeBodyJSON(w, "Пункт инструкции обновлён", itemToHTTP(it))
}

func (h *Handler) DeleteInstructionItem(w http.ResponseWriter, r *http.Request) {
	id := chi.URLParam(r, "id")
	if id == "" {
		http.Error(w, "ID пункта обязателен", http.StatusBadRequest)
		return
	}

	if err := h.db.DeleteInstructionItem(r.Context(), id); err != nil {
		if errors.Is(err, pgx.ErrNoRows) {
			http.Error(w, "Пункт не найден", http.StatusNotFound)
			return
		}
		h.logger.Error().Err(err).Str("id", id).Msg("failed to delete instruction item")
		http.Error(w, "Внутренняя ошибка сервера", http.StatusInternalServerError)
		return
	}
	h.writeBodyJSON(w, "Пункт инструкции удалён", map[string]string{"status": "ok"})
}

func (h *Handler) ReorderInstructionItems(w http.ResponseWriter, r *http.Request) {
	var req httpType.ReorderInstructionItemsRequest
	if err := h.readBodyJSON(w, r, &req); err != nil {
		return
	}
	if len(req.Order) == 0 {
		http.Error(w, "Список порядка обязателен", http.StatusBadRequest)
		return
	}

	items, err := h.db.ReorderInstructionItems(r.Context(), req.Order)
	if err != nil {
		h.logger.Error().Err(err).Msg("failed to reorder instruction items")
		http.Error(w, "Неверный список порядка", http.StatusBadRequest)
		return
	}
	h.writeBodyJSON(w, "Порядок пунктов обновлён", itemsToHTTP(items))
}

func itemToHTTP(it dbTypes.InstructionItem) httpType.InstructionItem {
	return httpType.InstructionItem{
		ID:        it.ID,
		Title:     it.Title,
		Body:      it.Body,
		Numbered:  it.Numbered,
		Position:  it.Position,
		UpdatedAt: it.UpdatedAt.UTC().Format(time.RFC3339),
	}
}

func itemsToHTTP(items []dbTypes.InstructionItem) []httpType.InstructionItem {
	result := make([]httpType.InstructionItem, len(items))
	for i, it := range items {
		result[i] = itemToHTTP(it)
	}
	return result
}
