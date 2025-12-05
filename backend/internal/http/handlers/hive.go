package handlers

import (
	"BeeIOT/internal/domain/models/dbTypes"
	"BeeIOT/internal/domain/models/httpType"
	"net/http"
)

func (h *Handler) CreateHive(w http.ResponseWriter, r *http.Request) {
	email, err := h.getEmailFromContext(w, r)
	if err != nil {
		return
	}

	var createData httpType.CreateHive
	if err := h.readBodyJSON(w, r, &createData); err != nil {
		return
	}

	if err := h.db.NewHive(r.Context(), email, createData.Name); err != nil {
		h.logger.Error().Err(err).Str("email", email).
			Str("hive_name", createData.Name).Msg("error creating hive")
		http.Error(w, "Внутренняя ошибка сервера", http.StatusInternalServerError)
		return
	}
	h.logger.Debug().Str("email", email).Str("hive_name", createData.Name).Msg("hive created")

	h.writeBodyJSON(w, "Улей успешно создан", nil)
}

func (h *Handler) GetHives(w http.ResponseWriter, r *http.Request) {
	email, err := h.getEmailFromContext(w, r)
	if err != nil {
		return
	}

	hives, err := h.db.GetHives(r.Context(), email)
	if err != nil {
		h.logger.Error().Err(err).Str("email", email).Msg("error getting hives")
		http.Error(w, "Внутренняя ошибка сервера", http.StatusInternalServerError)
		return
	}
	h.logger.Debug().Str("email", email).Int("hive_count", len(hives)).Msg("hives retrieved successfully")

	h.writeBodyJSON(w, "Список ульев успешно получен", hives)
}

func (h *Handler) GetHive(w http.ResponseWriter, r *http.Request) {
	email, err := h.getEmailFromContext(w, r)
	if err != nil {
		return
	}

	hiveName := r.URL.Query().Get("name")
	if hiveName == "" {
		h.logger.Error().Msg("no \"name\" in request")
		http.Error(w, "Параметр \"name\" обязателен", http.StatusBadRequest)
		return
	}

	hive, err := h.db.GetHiveByName(r.Context(), email, hiveName)
	if err != nil {
		h.logger.Error().Err(err).Str("email", email).Str("hive_name", hiveName).Msg("error getting hive")
		http.Error(w, "Улей не найден", http.StatusNotFound)
		return
	}
	h.logger.Debug().Str("email", email).Str("hive_name", hiveName).Msg("hive retrieved")

	h.writeBodyJSON(w, "Улей успешно получен", hive)
}

func (h *Handler) UpdateHive(w http.ResponseWriter, r *http.Request) {
	email, err := h.getEmailFromContext(w, r)
	if err != nil {
		return
	}

	var updateData httpType.UpdateHive
	if err := h.readBodyJSON(w, r, &updateData); err != nil {
		return
	}

	if updateData.OldName == "" {
		h.logger.Warn().Str("email", email).Msg("old hive name is empty")
		http.Error(w, "Старое имя улья не может быть пустым", http.StatusBadRequest)
		return
	}
	if updateData.NewName == "" {
		h.logger.Warn().Str("email", email).Msg("new hive name is empty")
		http.Error(w, "Новое имя улья не может быть пустым", http.StatusBadRequest)
		return
	}

	hive := dbTypes.Hive{
		NameHive: updateData.NewName,
		Email:    email,
	}
	if err := h.db.UpdateHive(r.Context(), updateData.OldName, hive); err != nil {
		h.logger.Error().Err(err).Str("email", email).
			Str("old_name", updateData.OldName).Str("new_name", updateData.NewName).Msg("error updating hive")
		http.Error(w, "Внутренняя ошибка сервера", http.StatusInternalServerError)
		return
	}
	h.logger.Debug().Str("email", email).Str("old_name", updateData.OldName).
		Str("new_name", updateData.NewName).Msg("hive updated")

	h.writeBodyJSON(w, "улей успешно обновлен", nil)
}

func (h *Handler) DeleteHive(w http.ResponseWriter, r *http.Request) {
	email, err := h.getEmailFromContext(w, r)
	if err != nil {
		return
	}

	var deleteData httpType.DeleteHive
	if err := h.readBodyJSON(w, r, &deleteData); err != nil {
		return
	}

	if deleteData.Name == "" {
		h.logger.Warn().Str("email", email).Msg("no \"name\" in request")
		http.Error(w, "Имя улья не может быть пустым", http.StatusBadRequest)
		return
	}

	if err := h.db.DeleteHive(r.Context(), email, deleteData.Name); err != nil {
		h.logger.Error().Err(err).Str("email", email).
			Str("hive_name", deleteData.Name).Msg("error deleting hive")
		http.Error(w, "Внутренняя ошибка сервера", http.StatusInternalServerError)
		return
	}
	h.logger.Debug().Str("email", email).Str("hive_name", deleteData.Name).
		Msg("hive deleted successfully")

	h.writeBodyJSON(w, "Улей успешно удален", nil)
}
