package handlers

import (
	"BeeIOT/internal/domain/models/dbTypes"
	"BeeIOT/internal/domain/models/httpType"
	"errors"
	"net/http"
	"time"

	"github.com/jackc/pgx/v5"
)

const (
	appDescTitleMax = 80
	appDescShortMax = 160
)

func (h *Handler) GetAppDescription(w http.ResponseWriter, r *http.Request) {
	d, err := h.db.GetAppDescription(r.Context())
	if err != nil {
		if errors.Is(err, pgx.ErrNoRows) {
			http.Error(w, "Описание приложения не задано", http.StatusNotFound)
			return
		}
		h.logger.Error().Err(err).Msg("failed to get app description")
		http.Error(w, "Внутренняя ошибка сервера", http.StatusInternalServerError)
		return
	}
	h.writeBodyJSON(w, "Описание приложения получено", appDescToHTTP(d))
}

func (h *Handler) UpdateAppDescription(w http.ResponseWriter, r *http.Request) {
	email, err := h.getEmailFromContext(w, r)
	if err != nil {
		return
	}

	var req httpType.UpdateAppDescriptionRequest
	if err := h.readBodyJSON(w, r, &req); err != nil {
		return
	}

	if req.Title == "" || len(req.Title) > appDescTitleMax {
		http.Error(w, "Заголовок: 1..80 символов", http.StatusBadRequest)
		return
	}
	if req.Short == "" || len(req.Short) > appDescShortMax {
		http.Error(w, "Краткое описание: 1..160 символов", http.StatusBadRequest)
		return
	}
	if req.Full == "" {
		http.Error(w, "Полное описание обязательно", http.StatusBadRequest)
		return
	}

	d, err := h.db.UpsertAppDescription(r.Context(), req, email)
	if err != nil {
		h.logger.Error().Err(err).Msg("failed to upsert app description")
		http.Error(w, "Внутренняя ошибка сервера", http.StatusInternalServerError)
		return
	}
	h.writeBodyJSON(w, "Описание приложения обновлено", appDescToHTTP(d))
}

func appDescToHTTP(d dbTypes.AppDescription) httpType.AppDescription {
	return httpType.AppDescription{
		Title:     d.Title,
		Short:     d.Short,
		Full:      d.Full,
		UpdatedAt: d.UpdatedAt.UTC().Format(time.RFC3339),
		UpdatedBy: d.UpdatedBy,
	}
}
