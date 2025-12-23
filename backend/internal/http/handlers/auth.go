package handlers

import (
	"BeeIOT/internal/domain/models/httpType"
	"BeeIOT/internal/domain/passwords"
	"net/http"
)

func (h *Handler) Registration(w http.ResponseWriter, r *http.Request) {
	var userData httpType.Registration
	if err := h.readBodyJSON(w, r, &userData); err != nil {
		return
	}

	exist, err := h.db.IsExistUser(r.Context(), userData.Email)
	if err != nil {
		h.logger.Error().Str("email", userData.Email).Err(err).Msg("failed to check user existence")
		http.Error(w, "Внутренняя ошибка сервера", http.StatusInternalServerError)
		return
	}
	if exist {
		h.logger.Warn().Str("email", userData.Email).Msg("user does not exist")
		http.Error(w, "Пользователь с таким email не зарегистрирован", http.StatusNotFound)
		return
	}

	confirmCode, err := h.conf.NewCode(userData.Email, userData.Password)
	if err != nil {
		h.logger.Error().Str("email", userData.Email).Err(err).Msg("failed to create new confirmation code")
		http.Error(w, "Внутренняя ошибка сервера", http.StatusInternalServerError)
		return
	}
	h.logger.Debug().Str("email", userData.Email).Str("code", confirmCode).
		Msg("new confirmation code created")

	if err := h.conf.Sender.SendConfirmationCode(userData.Email, confirmCode); err != nil {
		h.logger.Warn().Str("email", userData.Email).Err(err).Msg("failed to send confirmation code")
	}

	h.writeBodyJSON(w, "Код подтверждения отправлен на email", nil)
}

func (h *Handler) ConfirmRegistration(w http.ResponseWriter, r *http.Request) {
	var confirmData httpType.Confirm
	if err := h.readBodyJSON(w, r, &confirmData); err != nil {
		return
	}

	pswd, exist := h.conf.Verify(confirmData.Email, confirmData.Code)
	if !exist {
		h.logger.Warn().Str("email", confirmData.Email).Msg("invalid or expired confirmation code")
		http.Error(w, "Неверный или истекший код подтверждения", http.StatusUnauthorized)
		return
	}

	err := h.db.Registration(r.Context(), httpType.Registration{Email: confirmData.Email, Password: pswd})
	if err != nil {
		h.logger.Error().Err(err).Str("email", confirmData.Email).Msg("failed to register user in database")
		http.Error(w, "Внутренняя ошибка сервера", http.StatusInternalServerError)
		return
	}

	h.writeBodyJSON(w, "Ваш аккаунт успешно зарегистрирован", nil)
}

func (h *Handler) ConfirmChangePassword(w http.ResponseWriter, r *http.Request) {
	var confirmData httpType.Confirm
	if err := h.readBodyJSON(w, r, &confirmData); err != nil {
		return
	}

	pswd, exist := h.conf.Verify(confirmData.Email, confirmData.Code)
	if !exist {
		h.logger.Warn().Str("email", confirmData.Email).Msg("invalid or expired confirmation code")
		http.Error(w, "Неверный или истекший код подтверждения", http.StatusUnauthorized)
		return
	}
	h.logger.Debug().Str("email", confirmData.Email).Msg("new confirmation code created")

	if !h.checkExistenceUser(w, r, confirmData.Email) {
		return
	}

	err := h.db.ChangePassword(r.Context(), httpType.ChangePassword{
		Email: confirmData.Email, Password: pswd,
	})
	if err != nil {
		h.logger.Error().Err(err).Str("email", confirmData.Email).Msg("failed to change password in database")
		http.Error(w, "Внутренняя ошибка сервера", http.StatusInternalServerError)
		return
	}

	h.writeBodyJSON(w, "Пароль успешно изменен", nil)
}

func (h *Handler) ChangePassword(w http.ResponseWriter, r *http.Request) {
	var dataChange httpType.ChangePassword
	if err := h.readBodyJSON(w, r, &dataChange); err != nil {
		return
	}
	if !h.checkExistenceUser(w, r, dataChange.Email) {
		return
	}
	if err := h.inMemDb.DeleteAllJwts(r.Context(), dataChange.Email); err != nil {
		h.logger.Error().Err(err).Str("email", dataChange.Email).Msg("failed to delete user jwt")
		http.Error(w, "Внутренняя ошибка сервера", http.StatusInternalServerError)
		return
	}
	confirmCode, err := h.conf.NewCode(dataChange.Email, dataChange.Password)
	if err != nil {
		h.logger.Error().Err(err).Str("email", dataChange.Email).Msg("failed to generate confirmation code")
		http.Error(w, "Внутренняя ошибка сервера", http.StatusInternalServerError)
		return
	}
	h.logger.Debug().Str("email", dataChange.Email).Msg("new confirmation code created")

	if err := h.conf.Sender.SendConfirmationCode(dataChange.Email, confirmCode); err != nil {
		h.logger.Warn().Str("email", dataChange.Email).Err(err).Msg("failed to send confirmation code")
	}

	h.writeBodyJSON(w, "Код подтверждения отправлен на email", nil)
}

func (h *Handler) Login(w http.ResponseWriter, r *http.Request) {
	var loginData httpType.Login
	if err := h.readBodyJSON(w, r, &loginData); err != nil {
		return
	}

	pswdDb, err := h.db.Login(r.Context(), loginData)
	switch {
	case err != nil:
		h.logger.Error().Err(err).Str("email", loginData.Email).Msg("failed to login")
		http.Error(w, "Внутренняя ошибка сервера", http.StatusInternalServerError)
		return

	case pswdDb == "", !passwords.CheckPasswordHash(loginData.Password, pswdDb):
		h.logger.Warn().Str("email", loginData.Email).Msg("user not found or invalid password")
		http.Error(w, "Пользователь с таким email не "+
			"зарегистрирован или неверный пароль", http.StatusNotFound)
		return
	}
	token, err := h.tokenJWT.GenerateToken(loginData.Email)
	if err != nil {
		h.logger.Error().Err(err).Str("email", loginData.Email).Msg("failed to generate jwt token")
		http.Error(w, "Внутренняя ошибка сервера", http.StatusInternalServerError)
		return
	}
	if err := h.inMemDb.SetJwt(r.Context(), loginData.Email, token); err != nil {
		h.logger.Error().Err(err).Str("email", loginData.Email).
			Msg("failed to set jwt token in in-memory database")
		http.Error(w, "Внутренняя ошибка сервера", http.StatusInternalServerError)
		return
	}
	h.logger.Debug().Str("email", loginData.Email).Msg("new jwt token created successfully")

	h.writeBodyJSON(w, "Авторизация успешна", map[string]string{"token": token})
}

func (h *Handler) Logout(w http.ResponseWriter, r *http.Request) {
	email, err := h.getEmailFromContext(w, r)
	if err != nil {
		return
	}

	authHeader := r.Header.Get("Authorization")
	const bearerPrefix = "Bearer "
	token := authHeader[len(bearerPrefix):]

	if err := h.inMemDb.DeleteJwt(r.Context(), email, token); err != nil {
		h.logger.Error().Err(err).Str("email", email).Msg("failed to delete jwt from in-memory database")
		http.Error(w, "Внутренняя ошибка сервера", http.StatusInternalServerError)
		return
	}
	h.logger.Debug().Str("email", email).Msg("user logged out successfully")

	h.writeBodyJSON(w, "Вы успешно вышли из системы", nil)
}

func (h *Handler) DeleteUser(w http.ResponseWriter, r *http.Request) {
	email, err := h.getEmailFromContext(w, r)
	if err != nil {
		return
	}
	if err := h.db.DeleteUser(r.Context(), email); err != nil {
		h.logger.Error().Str("email", email).Err(err).Msg("failed to delete user from in-memory database")
		http.Error(w, "Внутренняя ошибка сервера", http.StatusInternalServerError)
		return
	}

	if err := h.inMemDb.DeleteAllJwts(r.Context(), email); err != nil {
		h.logger.Error().Err(err).Str("email", email).Msg("failed to delete jwt from in-memory database")
		http.Error(w, "Внутренняя ошибка сервера", http.StatusInternalServerError)
		return
	}
	h.logger.Debug().Str("email", email).Msg("user deleted successfully")

	h.writeBodyJSON(w, "Ваш аккаунт успешно удален", nil)
}

func (h *Handler) RefreshToken(w http.ResponseWriter, r *http.Request) {
	var userData httpType.Registration
	if err := h.readBodyJSON(w, r, &userData); err != nil {
		return
	}

	confirmCode, err := h.conf.NewCode(userData.Email, userData.Password)
	if err != nil {
		h.logger.Error().Str("email", userData.Email).Err(err).
			Msg("failed to create new confirmation code")
		http.Error(w, "Внутренняя ошибка сервера", http.StatusInternalServerError)
		return
	}
	h.logger.Debug().Str("email", userData.Email).Str("code", confirmCode).
		Msg("new confirmation code created")

	if err := h.conf.Sender.SendConfirmationCode(userData.Email, confirmCode); err != nil {
		h.logger.Warn().Str("email", userData.Email).Err(err).Msg("failed to send confirmation code")
	}

	h.writeBodyJSON(w, "Код подтверждения отправлен на email", nil)
}

func (h *Handler) checkExistenceUser(w http.ResponseWriter, r *http.Request, email string) bool {
	exist, err := h.db.IsExistUser(r.Context(), email)
	if err != nil {
		h.logger.Error().Str("email", email).Err(err).Msg("failed to check user existence")
		http.Error(w, "Внутренняя ошибка сервера", http.StatusInternalServerError)
		return false
	}
	if !exist {
		h.logger.Warn().Str("email", email).Msg("user does not exist")
		http.Error(w, "Пользователь с таким email не зарегистрирован", http.StatusNotFound)
		return false
	}
	return true
}
