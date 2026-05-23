package handlers

import (
	"BeeIOT/internal/domain/models/httpType"
	"net/http"
)

func (h *Handler) CreateHub(w http.ResponseWriter, r *http.Request) {
	email, err := h.getEmailFromContext(w, r)
	if err != nil {
		return
	}

	var createData httpType.CreateHub
	if err := h.readBodyJSON(w, r, &createData); err != nil {
		return
	}

	if createData.ID == "" {
		h.logger.Warn().Str("email", email).Msg("hub id is empty")
		http.Error(w, "Идентификатор хаба обязателен", http.StatusBadRequest)
		return
	}
	if createData.Name == "" {
		h.logger.Warn().Str("email", email).Msg("hub name is empty")
		http.Error(w, "Имя хаба обязательно", http.StatusBadRequest)
		return
	}

	if err := h.db.NewHub(r.Context(), email, createData.Name, createData.ID); err != nil {
		h.logger.Error().Err(err).Str("email", email).
			Str("hub_id", createData.ID).Msg("error creating hub")
		http.Error(w, "Внутренняя ошибка сервера", http.StatusInternalServerError)
		return
	}
	h.logger.Debug().Str("email", email).Str("hub_id", createData.ID).Msg("hub created")

	h.writeBodyJSON(w, "Хаб успешно создан", nil)
}

func (h *Handler) GetHubs(w http.ResponseWriter, r *http.Request) {
	email, err := h.getEmailFromContext(w, r)
	if err != nil {
		return
	}

	hubs, err := h.db.GetHubs(r.Context(), email)
	if err != nil {
		h.logger.Error().Err(err).Str("email", email).Msg("error getting hubs")
		http.Error(w, "Внутренняя ошибка сервера", http.StatusInternalServerError)
		return
	}

	result := make([]httpType.HubListItem, 0, len(hubs))
	for _, hb := range hubs {
		result = append(result, httpType.HubListItem{
			ID:   hb.Sensor,
			Name: hb.NameHub,
		})
	}
	h.writeBodyJSON(w, "Список хабов успешно получен", result)
}

func (h *Handler) GetHub(w http.ResponseWriter, r *http.Request) {
	email, err := h.getEmailFromContext(w, r)
	if err != nil {
		return
	}

	hubID := r.URL.Query().Get("id")
	if hubID == "" {
		h.logger.Error().Msg("no \"id\" in request")
		http.Error(w, "Параметр \"id\" обязателен", http.StatusBadRequest)
		return
	}

	hub, err := h.db.GetHubBySensor(r.Context(), email, hubID)
	if err != nil {
		h.logger.Error().Err(err).Str("email", email).Str("hub_id", hubID).Msg("error getting hub")
		http.Error(w, "Хаб не найден", http.StatusNotFound)
		return
	}

	h.writeBodyJSON(w, "Хаб успешно получен", httpType.HubDetails{
		ID:   hub.Sensor,
		Name: hub.NameHub,
	})
}

func (h *Handler) DeleteHub(w http.ResponseWriter, r *http.Request) {
	email, err := h.getEmailFromContext(w, r)
	if err != nil {
		return
	}

	var deleteData httpType.DeleteHub
	if err := h.readBodyJSON(w, r, &deleteData); err != nil {
		return
	}

	if deleteData.ID == "" {
		h.logger.Warn().Str("email", email).Msg("hub id is empty")
		http.Error(w, "Идентификатор хаба обязателен", http.StatusBadRequest)
		return
	}

	if err := h.db.DeleteHub(r.Context(), email, deleteData.ID); err != nil {
		h.logger.Error().Err(err).Str("email", email).
			Str("hub_id", deleteData.ID).Msg("error deleting hub")
		http.Error(w, "Внутренняя ошибка сервера", http.StatusInternalServerError)
		return
	}
	h.logger.Debug().Str("email", email).Str("hub_id", deleteData.ID).Msg("hub deleted")

	h.writeBodyJSON(w, "Хаб успешно удален", nil)
}

func (h *Handler) UpdateHub(w http.ResponseWriter, r *http.Request) {
	email, err := h.getEmailFromContext(w, r)
	if err != nil {
		return
	}

	var updateData httpType.UpdateHub
	if err := h.readBodyJSON(w, r, &updateData); err != nil {
		return
	}

	if updateData.ID == "" {
		h.logger.Warn().Str("email", email).Msg("hub id is empty")
		http.Error(w, "Идентификатор хаба не может быть пустым", http.StatusBadRequest)
		return
	}

	if err := h.db.UpdateHub(r.Context(), email, updateData); err != nil {
		h.logger.Error().Err(err).Str("email", email).
			Str("hub_id", updateData.ID).Msg("error updating hub")
		http.Error(w, "Внутренняя ошибка сервера", http.StatusInternalServerError)
		return
	}
	h.logger.Debug().Str("email", email).Str("hub_id", updateData.ID).Msg("hub updated")

	h.writeBodyJSON(w, "Хаб успешно обновлен", nil)
}