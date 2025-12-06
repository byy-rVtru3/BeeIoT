package handlers

import (
	"BeeIOT/internal/domain/confirm"
	"BeeIOT/internal/domain/interfaces"
	"BeeIOT/internal/domain/jwtToken"
	"BeeIOT/internal/domain/mqtt"
	"encoding/json"
	"net/http"

	"github.com/rs/zerolog"
)

type Handler struct {
	db       interfaces.DB
	conf     *confirm.Confirm
	tokenJWT *jwtToken.JWTToken
	inMemDb  interfaces.InMemoryDB
	logger   zerolog.Logger
	mqtt     *mqtt.Client
}

func NewHandler(db interfaces.DB, codeSender interfaces.ConfirmSender,
	inMem interfaces.InMemoryDB, mqtt *mqtt.Client, logger zerolog.Logger) (*Handler, error) {
	conf, err := confirm.NewConfirm(codeSender)
	if err != nil {
		logger.Error().Err(err).Msg("failed to create confirm service")
		return nil, err
	}
	logger.Info().Msg("confirm service created successfully")

	jw, err := jwtToken.NewJWTToken()
	if err != nil {
		logger.Error().Err(err).Msg("failed to create jwt token")
		return nil, err
	}
	logger.Info().Msg("jwt token created successfully")
	return &Handler{db: db, conf: conf, tokenJWT: jw, inMemDb: inMem, logger: logger, mqtt: mqtt}, nil
}

type Response struct {
	Status  string `json:"status"`
	Message string `json:"message"`
	Data    any    `json:"data,omitempty"`
}

func (h *Handler) readBodyJSON(w http.ResponseWriter, r *http.Request, v any) error {
	if err := json.NewDecoder(r.Body).Decode(v); err != nil {
		h.logger.Error().Err(err).Msg("error decoding request body")
		http.Error(w, "Неверный формат JSON", http.StatusBadRequest)
		return err
	}
	h.logger.Debug().Msg("request body decoded successfully")
	return nil
}

func (h *Handler) writeBodyJSON(w http.ResponseWriter, message string, data any) {
	resp := Response{
		Status:  "ok",
		Message: message,
		Data:    data,
	}
	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(http.StatusOK)
	if err := json.NewEncoder(w).Encode(resp); err != nil {
		h.logger.Warn().Err(err).Msg("error encoding response body")
	}
}

func (h *Handler) getEmailFromContext(w http.ResponseWriter, r *http.Request) (string, error) {
	email := r.Context().Value("email").(string)
	if email == "" {
		h.logger.Error().Msg("no email in context")
		http.Error(w, "Внутренняя ошибка сервера", http.StatusInternalServerError)
		return "", http.ErrNoCookie
	}
	return email, nil
}
