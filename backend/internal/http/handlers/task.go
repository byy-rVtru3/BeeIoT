package handlers

import (
	"BeeIOT/internal/domain/models/httpType"
	"net/http"
)

func (h *Handler) CreateTask(w http.ResponseWriter, r *http.Request) {
	email, err := h.getEmailFromContext(w, r)
	if err != nil {
		return
	}

	var req httpType.CreateTaskRequest
	if err := h.readBodyJSON(w, r, &req); err != nil {
		return
	}

	if req.HiveName == "" {
		h.logger.Warn().Str("email", email).Msg("hive name is empty")
		http.Error(w, "Название улья обязательно", http.StatusBadRequest)
		return
	}
	if req.Title == "" {
		h.logger.Warn().Str("email", email).Msg("title is empty")
		http.Error(w, "Заголовок работы обязателен", http.StatusBadRequest)
		return
	}

	// Проверяем существование улья
	_, err = h.db.GetHiveByName(r.Context(), email, req.HiveName, nil)
	if err != nil {
		h.logger.Warn().Str("email", email).Str("hive_name", req.HiveName).Msg("hive not found")
		http.Error(w, "Улей не найден", http.StatusBadRequest)
		return
	}

	taskID, err := h.db.CreateTask(r.Context(), email, req)
	if err != nil {
		h.logger.Error().Err(err).Str("email", email).Msg("failed to create task")
		http.Error(w, "Внутренняя ошибка сервера", http.StatusInternalServerError)
		return
	}

	task, _ := h.db.GetTaskByID(r.Context(), taskID)
	h.logger.Debug().Str("email", email).Str("task_id", taskID).Msg("task created")

	h.writeBodyJSON(w, "Работа успешно добавлена", httpType.TaskItem{
		ID:          task.ID,
		HiveName:    task.HiveName,
		Title:       task.Title,
		Description: task.Description,
		CreatedAt:   task.CreatedAt.Unix(),
	})
}

func (h *Handler) GetTasks(w http.ResponseWriter, r *http.Request) {
	email, err := h.getEmailFromContext(w, r)
	if err != nil {
		return
	}

	hiveName := r.URL.Query().Get("hive_name")

	tasks, err := h.db.GetTasks(r.Context(), email, hiveName)
	if err != nil {
		h.logger.Error().Err(err).Str("email", email).Msg("failed to get tasks")
		http.Error(w, "Внутренняя ошибка сервера", http.StatusInternalServerError)
		return
	}

	result := make([]httpType.TaskItem, len(tasks))
	for i, task := range tasks {
		result[i] = httpType.TaskItem{
			ID:          task.ID,
			HiveName:    task.HiveName,
			Title:       task.Title,
			Description: task.Description,
			CreatedAt:   task.CreatedAt.Unix(),
		}
	}

	h.writeBodyJSON(w, "Список работ получен", result)
}

func (h *Handler) UpdateTask(w http.ResponseWriter, r *http.Request) {
	email, err := h.getEmailFromContext(w, r)
	if err != nil {
		return
	}

	var req httpType.UpdateTaskRequest
	if err := h.readBodyJSON(w, r, &req); err != nil {
		return
	}

	if req.ID == "" {
		h.logger.Warn().Str("email", email).Msg("task id is empty")
		http.Error(w, "ID работы обязателен", http.StatusBadRequest)
		return
	}

	if err := h.db.UpdateTask(r.Context(), email, req); err != nil {
		h.logger.Warn().Err(err).Str("email", email).Str("task_id", req.ID).Msg("failed to update task")
		http.Error(w, "Нет прав на редактирование этой работы", http.StatusForbidden)
		return
	}

	h.logger.Debug().Str("email", email).Str("task_id", req.ID).Msg("task updated")
	h.writeBodyJSON(w, "Запись о работе успешно обновлена", nil)
}

func (h *Handler) DeleteTask(w http.ResponseWriter, r *http.Request) {
	email, err := h.getEmailFromContext(w, r)
	if err != nil {
		return
	}

	var req httpType.DeleteTaskRequest
	if err := h.readBodyJSON(w, r, &req); err != nil {
		return
	}

	if req.ID == "" {
		h.logger.Warn().Str("email", email).Msg("task id is empty")
		http.Error(w, "ID работы обязателен", http.StatusBadRequest)
		return
	}

	if err := h.db.DeleteTask(r.Context(), email, req.ID); err != nil {
		h.logger.Warn().Err(err).Str("email", email).Str("task_id", req.ID).Msg("failed to delete task")
		http.Error(w, "Нет прав на удаление этой работы", http.StatusForbidden)
		return
	}

	h.logger.Debug().Str("email", email).Str("task_id", req.ID).Msg("task deleted")
	h.writeBodyJSON(w, "Запись о работе успешно удалена", nil)
}
