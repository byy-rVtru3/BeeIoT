package handlers

import (
	"BeeIOT/internal/domain/models/dbTypes"
	"BeeIOT/internal/domain/models/httpType"
	"encoding/json"
	"log/slog"
	"net/http"
)

func (h *Handler) CreateHive(w http.ResponseWriter, r *http.Request) {

	email := r.Context().Value("email").(string)
	if email == "" {
		slog.Error("Email not found in context",
			"module", "handlers",
			"function", "CreateHive")
		http.Error(w, "Внутренняя ошибка сервера", http.StatusInternalServerError)
		return
	}

	var createData httpType.CreateHive
	if err := json.NewDecoder(r.Body).Decode(&createData); err != nil {
		slog.Error("Failed to decode JSON request body",
			"module", "handlers",
			"function", "CreateHive",
			"error", err)
		http.Error(w, "Неверный формат JSON", http.StatusBadRequest)
		return
	}
	slog.Debug("JSON request decoded successfully",
		"module", "handlers",
		"function", "CreateHive",
		"email", email,
		"hive_name", createData.Name)

	if createData.Name == "" {
		slog.Warn("Hive name is empty",
			"module", "handlers",
			"function", "CreateHive",
			"email", email)
		http.Error(w, "Имя улья не может быть пустым", http.StatusBadRequest)
		return
	}

	err := h.db.NewHive(r.Context(), email, createData.Name)
	if err != nil {
		slog.Error("Failed to create hive in database",
			"module", "handlers",
			"function", "CreateHive",
			"email", email,
			"hive_name", createData.Name,
			"error", err)
		http.Error(w, "Внутренняя ошибка сервера", http.StatusInternalServerError)
		return
	}
	slog.Debug("Hive created successfully",
		"module", "handlers",
		"function", "CreateHive",
		"email", email,
		"hive_name", createData.Name)

	resp := Response{
		Status:  "ok",
		Message: "Улей успешно создан",
		Data:    nil,
	}
	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(http.StatusOK)
	err = json.NewEncoder(w).Encode(resp)
	if err != nil {
		slog.Warn("Failed to encode JSON response",
			"module", "handlers",
			"function", "CreateHive",
			"error", err)
	}
}

func (h *Handler) GetHives(w http.ResponseWriter, r *http.Request) {
	email := r.Context().Value("email").(string)
	if email == "" {
		slog.Error("Email not found in context",
			"module", "handlers",
			"function", "GetHives")
		http.Error(w, "Внутренняя ошибка сервера", http.StatusInternalServerError)
		return
	}

	hives, err := h.db.GetHives(r.Context(), email)
	if err != nil {
		slog.Error("Failed to get hives from database",
			"module", "handlers",
			"function", "GetHives",
			"email", email,
			"error", err)
		http.Error(w, "Внутренняя ошибка сервера", http.StatusInternalServerError)
		return
	}
	slog.Debug("Hives retrieved successfully",
		"module", "handlers",
		"function", "GetHives",
		"email", email,
		"count", len(hives))

	resp := Response{
		Status:  "ok",
		Message: "Список ульев успешно получен",
		Data:    hives,
	}
	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(http.StatusOK)
	err = json.NewEncoder(w).Encode(resp)
	if err != nil {
		slog.Warn("Failed to encode JSON response",
			"module", "handlers",
			"function", "GetHives",
			"error", err)
	}
}

func (h *Handler) GetHive(w http.ResponseWriter, r *http.Request) {
	email := r.Context().Value("email").(string)
	if email == "" {
		slog.Error("Email not found in context",
			"module", "handlers",
			"function", "GetHive")
		http.Error(w, "Внутренняя ошибка сервера", http.StatusInternalServerError)
		return
	}

	hiveName := r.URL.Query().Get("name")
	if hiveName == "" {
		slog.Warn("Hive name parameter is missing",
			"module", "handlers",
			"function", "GetHive",
			"email", email)
		http.Error(w, "Параметр 'name' обязателен", http.StatusBadRequest)
		return
	}

	hive, err := h.db.GetHiveByName(r.Context(), email, hiveName)
	if err != nil {
		slog.Error("Failed to get hive from database",
			"module", "handlers",
			"function", "GetHive",
			"email", email,
			"hive_name", hiveName,
			"error", err)
		http.Error(w, "Улей не найден", http.StatusNotFound)
		return
	}
	slog.Debug("Hive retrieved successfully",
		"module", "handlers",
		"function", "GetHive",
		"email", email,
		"hive_name", hiveName)

	resp := Response{
		Status:  "ok",
		Message: "Улей успешно получен",
		Data:    hive,
	}
	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(http.StatusOK)
	err = json.NewEncoder(w).Encode(resp)
	if err != nil {
		slog.Warn("Failed to encode JSON response",
			"module", "handlers",
			"function", "GetHive",
			"error", err)
	}
}

func (h *Handler) UpdateHive(w http.ResponseWriter, r *http.Request) {
	email := r.Context().Value("email").(string)
	if email == "" {
		slog.Error("Email not found in context",
			"module", "handlers",
			"function", "UpdateHive")
		http.Error(w, "Внутренняя ошибка сервера", http.StatusInternalServerError)
		return
	}

	var updateData httpType.UpdateHive
	if err := json.NewDecoder(r.Body).Decode(&updateData); err != nil {
		slog.Error("Failed to decode JSON request body",
			"module", "handlers",
			"function", "UpdateHive",
			"error", err)
		http.Error(w, "Неверный формат JSON", http.StatusBadRequest)
		return
	}
	slog.Debug("JSON request decoded successfully",
		"module", "handlers",
		"function", "UpdateHive",
		"email", email,
		"old_name", updateData.OldName,
		"new_name", updateData.NewName)

	if updateData.OldName == "" {
		slog.Warn("Old hive name is empty",
			"module", "handlers",
			"function", "UpdateHive",
			"email", email)
		http.Error(w, "Старое имя улья не может быть пустым", http.StatusBadRequest)
		return
	}
	if updateData.NewName == "" {
		slog.Warn("New hive name is empty",
			"module", "handlers",
			"function", "UpdateHive",
			"email", email)
		http.Error(w, "Новое имя улья не может быть пустым", http.StatusBadRequest)
		return
	}

	hive := dbTypes.Hive{
		NameHive: updateData.NewName,
		Email:    email,
	}
	err := h.db.UpdateHive(r.Context(), updateData.OldName, hive)
	if err != nil {
		slog.Error("Failed to update hive in database",
			"module", "handlers",
			"function", "UpdateHive",
			"email", email,
			"old_name", updateData.OldName,
			"new_name", updateData.NewName,
			"error", err)
		http.Error(w, "Внутренняя ошибка сервера", http.StatusInternalServerError)
		return
	}
	slog.Debug("Hive updated successfully",
		"module", "handlers",
		"function", "UpdateHive",
		"email", email,
		"old_name", updateData.OldName,
		"new_name", updateData.NewName)

	resp := Response{
		Status:  "ok",
		Message: "Улей успешно обновлен",
		Data:    nil,
	}
	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(http.StatusOK)
	err = json.NewEncoder(w).Encode(resp)
	if err != nil {
		slog.Warn("Failed to encode JSON response",
			"module", "handlers",
			"function", "UpdateHive",
			"error", err)
	}
}

func (h *Handler) DeleteHive(w http.ResponseWriter, r *http.Request) {
	email := r.Context().Value("email").(string)
	if email == "" {
		slog.Error("Email not found in context",
			"module", "handlers",
			"function", "DeleteHive")
		http.Error(w, "Внутренняя ошибка сервера", http.StatusInternalServerError)
		return
	}

	var deleteData httpType.DeleteHive
	if err := json.NewDecoder(r.Body).Decode(&deleteData); err != nil {
		slog.Error("Failed to decode JSON request body",
			"module", "handlers",
			"function", "DeleteHive",
			"error", err)
		http.Error(w, "Неверный формат JSON", http.StatusBadRequest)
		return
	}
	slog.Debug("JSON request decoded successfully",
		"module", "handlers",
		"function", "DeleteHive",
		"email", email,
		"hive_name", deleteData.Name)

	if deleteData.Name == "" {
		slog.Warn("Hive name is empty",
			"module", "handlers",
			"function", "DeleteHive",
			"email", email)
		http.Error(w, "Имя улья не может быть пустым", http.StatusBadRequest)
		return
	}

	err := h.db.DeleteHive(r.Context(), email, deleteData.Name)
	if err != nil {
		slog.Error("Failed to delete hive from database",
			"module", "handlers",
			"function", "DeleteHive",
			"email", email,
			"hive_name", deleteData.Name,
			"error", err)
		http.Error(w, "Внутренняя ошибка сервера", http.StatusInternalServerError)
		return
	}
	slog.Debug("Hive deleted successfully",
		"module", "handlers",
		"function", "DeleteHive",
		"email", email,
		"hive_name", deleteData.Name)

	resp := Response{
		Status:  "ok",
		Message: "Улей успешно удален",
		Data:    nil,
	}
	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(http.StatusOK)
	err = json.NewEncoder(w).Encode(resp)
	if err != nil {
		slog.Warn("Failed to encode JSON response",
			"module", "handlers",
			"function", "DeleteHive",
			"error", err)
	}
}
