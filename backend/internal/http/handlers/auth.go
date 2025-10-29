package handlers

import (
	"BeeIOT/internal/domain/confirm"
	"BeeIOT/internal/domain/interfaces"
	"BeeIOT/internal/domain/jwtToken"
	"BeeIOT/internal/domain/types/httpType"
	"encoding/json"
	"log/slog"
	"net/http"
)

type Handler struct {
	db       interfaces.DB
	conf     *confirm.Confirm
	tokenJWT *jwtToken.JWTToken
}

func NewHandler(db interfaces.DB, codeSender interfaces.ConfirmSender) (*Handler, error) {
	conf, err := confirm.NewConfirm(codeSender)
	if err != nil {
		slog.Error("Failed to create confirm service",
			"module", "handlers",
			"function", "NewHandler",
			"error", err)
		return nil, err
	}
	slog.Debug("Confirm service created successfully",
		"module", "handlers",
		"function", "NewHandler")

	jw, err := jwtToken.NewJWTToken()
	if err != nil {
		slog.Error("Failed to create JWT token service",
			"module", "handlers",
			"function", "NewHandler",
			"error", err)
		return nil, err
	}
	slog.Debug("JWT token service created successfully",
		"module", "handlers",
		"function", "NewHandler")

	return &Handler{db: db, conf: conf, tokenJWT: jw}, nil
}

type Response struct {
	Status  string `json:"status"`
	Message string `json:"message"`
	Data    any    `json:"data,omitempty"`
}

func (h *Handler) Registration(w http.ResponseWriter, r *http.Request) {
	var userData httpType.Registration
	if err := json.NewDecoder(r.Body).Decode(&userData); err != nil {
		slog.Error("Failed to decode JSON request body",
			"module", "handlers",
			"function", "Registration",
			"error", err)
		http.Error(w, "Неверный формат JSON", http.StatusBadRequest)
		return
	}
	slog.Debug("JSON request decoded successfully",
		"module", "handlers",
		"function", "Registration",
		"email", userData.Email)

	exist, err := h.db.IsExistUser(r.Context(), userData.Email)
	if err != nil {
		slog.Error("Failed to check if user exists",
			"module", "handlers",
			"function", "Registration",
			"email", userData.Email,
			"error", err)
		http.Error(w, "Внутренняя ошибка сервера", http.StatusInternalServerError)
		return
	}
	if exist {
		slog.Warn("User already exists",
			"module", "handlers",
			"function", "Registration",
			"email", userData.Email)
		http.Error(w, "Пользователь с таким email уже зарегистрирован", http.StatusConflict)
		return
	}

	confirmCode, err := h.conf.NewCode(userData.Email, userData.Password)
	if err != nil {
		slog.Error("Failed to generate confirmation code",
			"module", "handlers",
			"function", "Registration",
			"email", userData.Email,
			"error", err)
		http.Error(w, "Внутренняя ошибка сервера", http.StatusInternalServerError)
		return
	}
	slog.Debug("Confirmation code generated",
		"module", "handlers",
		"function", "Registration",
		"email", userData.Email)

	if err := h.conf.Sender.SendConfirmationCode(userData.Email, confirmCode); err != nil {
		slog.Warn("Failed to send confirmation code",
			"module", "handlers",
			"function", "Registration",
			"email", userData.Email,
			"error", err)
	}

	resp := Response{
		Status:  "ok",
		Message: "Код подтверждения отправлен на email",
		Data:    nil,
	}
	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(http.StatusOK)
	err = json.NewEncoder(w).Encode(resp)
	if err != nil {
		slog.Warn("Failed to encode JSON response",
			"module", "handlers",
			"function", "Registration",
			"error", err)
	}
}

func (h *Handler) ConfirmRegistration(w http.ResponseWriter, r *http.Request) {
	var confirmData httpType.Confirm
	if err := json.NewDecoder(r.Body).Decode(&confirmData); err != nil {
		slog.Error("Failed to decode JSON request body",
			"module", "handlers",
			"function", "ConfirmRegistration",
			"error", err)
		http.Error(w, "Неверный формат JSON", http.StatusBadRequest)
		return
	}
	slog.Debug("JSON request decoded successfully",
		"module", "handlers",
		"function", "ConfirmRegistration",
		"email", confirmData.Email)

	pswd, exist := h.conf.Verify(confirmData.Email, confirmData.Code)
	if !exist {
		slog.Warn("Invalid or expired confirmation code",
			"module", "handlers",
			"function", "ConfirmRegistration",
			"email", confirmData.Email)
		http.Error(w, "Неверный или истекший код подтверждения", http.StatusUnauthorized)
		return
	}
	slog.Debug("Confirmation code verified successfully",
		"module", "handlers",
		"function", "ConfirmRegistration",
		"email", confirmData.Email)

	err := h.db.Registration(r.Context(), httpType.Registration{Email: confirmData.Email, Password: pswd})
	if err != nil {
		slog.Error("Failed to register user in database",
			"module", "handlers",
			"function", "ConfirmRegistration",
			"email", confirmData.Email,
			"error", err)
		http.Error(w, "Внутренняя ошибка сервера", http.StatusInternalServerError)
		return
	}

	resp := Response{
		Status:  "ok",
		Message: "Ваш аккаунт успешно зарегистрирован",
		Data:    nil,
	}
	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(http.StatusOK)
	err = json.NewEncoder(w).Encode(resp)
	if err != nil {
		slog.Warn("Failed to encode JSON response",
			"module", "handlers",
			"function", "ConfirmRegistration",
			"error", err)
	}
}

func (h *Handler) ConfirmChangePassword(w http.ResponseWriter, r *http.Request) {
	var confirmData httpType.Confirm
	if err := json.NewDecoder(r.Body).Decode(&confirmData); err != nil {
		slog.Error("Failed to decode JSON request body",
			"module", "handlers",
			"function", "ConfirmChangePassword",
			"error", err)
		http.Error(w, "Неверный формат JSON", http.StatusBadRequest)
		return
	}
	slog.Debug("JSON request decoded successfully",
		"module", "handlers",
		"function", "ConfirmChangePassword",
		"email", confirmData.Email)

	pswd, exist := h.conf.Verify(confirmData.Email, confirmData.Code)
	if !exist {
		slog.Warn("Invalid or expired confirmation code",
			"module", "handlers",
			"function", "ConfirmChangePassword",
			"email", confirmData.Email)
		http.Error(w, "Неверный или истекший код подтверждения", http.StatusUnauthorized)
		return
	}
	slog.Debug("Confirmation code verified successfully",
		"module", "handlers",
		"function", "ConfirmChangePassword",
		"email", confirmData.Email)

	exist, err := h.db.IsExistUser(r.Context(), confirmData.Email)
	if err != nil {
		slog.Error("Failed to check if user exists",
			"module", "handlers",
			"function", "ConfirmChangePassword",
			"email", confirmData.Email,
			"error", err)
		http.Error(w, "Внутренняя ошибка сервера", http.StatusInternalServerError)
		return
	}
	if !exist {
		slog.Warn("User not found",
			"module", "handlers",
			"function", "ConfirmChangePassword",
			"email", confirmData.Email)
		http.Error(w, "Пользователь с таким email не зарегистрирован", http.StatusNotFound)
		return
	}

	err = h.db.ChangePassword(r.Context(), httpType.ChangePassword{Email: confirmData.Email, Password: pswd})
	if err != nil {
		slog.Error("Failed to change password in database",
			"module", "handlers",
			"function", "ConfirmChangePassword",
			"email", confirmData.Email,
			"error", err)
		http.Error(w, "Внутренняя ошибка сервера", http.StatusInternalServerError)
		return
	}

	resp := Response{
		Status:  "ok",
		Message: "Пароль успешно изменен",
		Data:    nil,
	}
	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(http.StatusOK)
	err = json.NewEncoder(w).Encode(resp)
	if err != nil {
		slog.Warn("Failed to encode JSON response",
			"module", "handlers",
			"function", "ConfirmChangePassword",
			"error", err)
	}
}

func (h *Handler) ExistUser(w http.ResponseWriter, r *http.Request) {
	email := r.URL.Query().Get("email")
	if email == "" {
		slog.Error("Email parameter is required",
			"module", "handlers",
			"function", "ExistUser")
		http.Error(w, "Параметр email обязателен", http.StatusBadRequest)
		return
	}
	slog.Debug("Checking user existence",
		"module", "handlers",
		"function", "ExistUser",
		"email", email)

	exist, err := h.db.IsExistUser(r.Context(), email)
	if err != nil {
		slog.Error("Failed to check if user exists",
			"module", "handlers",
			"function", "ExistUser",
			"email", email,
			"error", err)
		http.Error(w, "Внутренняя ошибка сервера", http.StatusInternalServerError)
		return
	}

	resp := Response{
		Status:  "ok",
		Message: "Проверка существования пользователя выполнена",
		Data:    map[string]bool{"exist": exist},
	}
	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(http.StatusOK)
	err = json.NewEncoder(w).Encode(resp)
	if err != nil {
		slog.Warn("Failed to encode JSON response",
			"module", "handlers",
			"function", "ExistUser",
			"error", err)
	}
}

func (h *Handler) Login(w http.ResponseWriter, r *http.Request) {
	var loginData httpType.Login
	if err := json.NewDecoder(r.Body).Decode(&loginData); err != nil {
		slog.Error("Failed to decode JSON request body",
			"module", "handlers",
			"function", "Login",
			"error", err)
		http.Error(w, "Неверный формат JSON", http.StatusBadRequest)
		return
	}
	slog.Debug("JSON request decoded successfully",
		"module", "handlers",
		"function", "Login",
		"email", loginData.Email)

	exist, err := h.db.Login(r.Context(), loginData)
	if err != nil {
		slog.Error("Failed to authenticate user",
			"module", "handlers",
			"function", "Login",
			"email", loginData.Email,
			"error", err)
		http.Error(w, "Внутренняя ошибка сервера", http.StatusInternalServerError)
		return
	}
	if !exist {
		slog.Warn("Authentication failed - user not found or invalid password",
			"module", "handlers",
			"function", "Login",
			"email", loginData.Email)
		http.Error(w, "Пользователь с таким email не "+
			"зарегистрирован или неверный пароль", http.StatusNotFound)
		return
	}

	token, err := h.tokenJWT.GenerateToken(loginData.Email)
	if err != nil {
		slog.Error("Failed to generate JWT token",
			"module", "handlers",
			"function", "Login",
			"email", loginData.Email,
			"error", err)
		http.Error(w, "Внутренняя ошибка сервера", http.StatusInternalServerError)
		return
	}
	slog.Debug("JWT token generated successfully",
		"module", "handlers",
		"function", "Login",
		"email", loginData.Email)

	resp := Response{
		Status:  "ok",
		Message: "Авторизация успешна",
		Data:    map[string]string{"token": token},
	}
	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(http.StatusOK)
	err = json.NewEncoder(w).Encode(resp)
	if err != nil {
		slog.Warn("Failed to encode JSON response",
			"module", "handlers",
			"function", "Login",
			"error", err)
	}
}
