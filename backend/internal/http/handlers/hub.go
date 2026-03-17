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

	if createData.Name == "" {
		h.logger.Warn().Str("email", email).Msg("hub name is empty")
		http.Error(w, "Имя хаба обязательно", http.StatusBadRequest)
		return
	}

	if err := h.db.NewHub(r.Context(), email, createData.Name, createData.Sensor); err != nil {
		h.logger.Error().Err(err).Str("email", email).
			Str("hub_name", createData.Name).Msg("error creating hub")
		http.Error(w, "Внутренняя ошибка сервера", http.StatusInternalServerError)
		return
	}
	h.logger.Debug().Str("email", email).Str("hub_name", createData.Name).Msg("hub created")

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
			Name:   hb.NameHub,
			Sensor: hb.Sensor,
		})
	}
	h.writeBodyJSON(w, "Список хабов успешно получен", result)
}

func (h *Handler) GetHub(w http.ResponseWriter, r *http.Request) {
	email, err := h.getEmailFromContext(w, r)
	if err != nil {
		return
	}

	hubName := r.URL.Query().Get("name")
	if hubName == "" {
		h.logger.Error().Msg("no \"name\" in request")
		http.Error(w, "Параметр \"name\" обязателен", http.StatusBadRequest)
		return
	}

	hub, err := h.db.GetHubByName(r.Context(), email, hubName)
	if err != nil {
		h.logger.Error().Err(err).Str("email", email).Str("hub_name", hubName).Msg("error getting hub")
		http.Error(w, "Хаб не найден", http.StatusNotFound)
		return
	}

	h.writeBodyJSON(w, "Хаб успешно получен", httpType.HubDetails{
		Name:   hub.NameHub,
		Sensor: hub.Sensor,
	})
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

	if updateData.OldName == "" {
		h.logger.Warn().Str("email", email).Msg("old hub name is empty")
		http.Error(w, "Старое имя хаба не может быть пустым", http.StatusBadRequest)
		return
	}

	if err := h.db.UpdateHub(r.Context(), email, updateData); err != nil {
		h.logger.Error().Err(err).Str("email", email).
			Str("old_name", updateData.OldName).Msg("error updating hub")
		http.Error(w, "Внутренняя ошибка сервера", http.StatusInternalServerError)
		return
	}
	h.logger.Debug().Str("email", email).Str("old_name", updateData.OldName).Msg("hub updated")

	h.writeBodyJSON(w, "Хаб успешно обновлен", nil)
}
