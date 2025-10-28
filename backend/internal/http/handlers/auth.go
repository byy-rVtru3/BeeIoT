package handlers

import (
	"BeeIOT/internal/domain/confirm"
	"BeeIOT/internal/domain/interfaces"
	"BeeIOT/internal/domain/types/httpType"
	"encoding/json"
	"net/http"
)

type Handler struct {
	db   interfaces.DB
	conf *confirm.Confirm
}

func NewHandler(db interfaces.DB, codeSender interfaces.ConfirmSender) (*Handler, error) {
	conf, err := confirm.NewConfirm(codeSender)
	if err != nil {
		return nil, err
	}
	return &Handler{db: db, conf: conf}, nil
}

type Response struct {
	Status  string `json:"status"`
	Message string `json:"message"`
	Data    any    `json:"data,omitempty"`
}

func (h *Handler) Registration(w http.ResponseWriter, r *http.Request) {
	var userData httpType.Registration
	if err := json.NewDecoder(r.Body).Decode(&userData); err != nil {
		http.Error(w, "Неверный формат JSON", http.StatusBadRequest) // логировать
		return
	}
	exist, err := h.db.IsExistUser(r.Context(), userData.Email)
	if err != nil {
		http.Error(w, "Внутренняя ошибка сервера", http.StatusInternalServerError)
		return
	}
	if exist {
		http.Error(w, "Пользователь с таким email уже зарегистрирован", http.StatusConflict)
		return
	}
	confirmCode := h.conf.NewCode(userData.Email)
	if h.conf.Sender.SendConfirmationCode(userData.Email, confirmCode) != nil {
		// логировать, но не возвращать ошибку пользователю
	}
	resp := Response{
		Status:  "ok",
		Message: "Код подтверждения отправлен на email",
		Data:    map[string]string{"code": confirmCode},
	}
	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(http.StatusOK)
	_ = json.NewEncoder(w).Encode(resp) // логировать ошибку
}

func (h *Handler) ConfirmRegistration(w http.ResponseWriter, r *http.Request) {
	var confirmData httpType.Confirm
	if err := json.NewDecoder(r.Body).Decode(&confirmData); err != nil {
		http.Error(w, "Неверный формат JSON", http.StatusBadRequest) // логировать
		return
	}
	if !h.conf.Verify(confirmData.Email, confirmData.Code) {
		http.Error(w, "Неверный или истекший код подтверждения", http.StatusUnauthorized)
		return
	}
	err := h.db.Registration(r.Context(), httpType.Registration{Email: confirmData.Email})
	if err != nil {
		http.Error(w, "Внутренняя ошибка сервера", http.StatusInternalServerError)
	}
	resp := Response{
		Status:  "ok",
		Message: "Ваш аккаунт успешно зарегистрирован",
		Data:    nil,
	}
	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(http.StatusOK)
	_ = json.NewEncoder(w).Encode(resp) // логировать ошибку
}

func (h *Handler) ConfirmChangePassword(w http.ResponseWriter, r *http.Request) {

}
