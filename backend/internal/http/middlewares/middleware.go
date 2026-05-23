package middlewares

import (
	"BeeIOT/internal/domain/interfaces"
	"BeeIOT/internal/domain/jwtToken"
	"context"
	"errors"
	"net/http"

	"github.com/golang-jwt/jwt/v5"
	"github.com/rs/zerolog"
)

type MiddleWares struct {
	db      interfaces.DB
	inMemDb interfaces.InMemoryDB
	jwt     *jwtToken.JWTToken
	logger  zerolog.Logger
}

func NewMiddleWares(db interfaces.DB, inMem interfaces.InMemoryDB, logger zerolog.Logger) (*MiddleWares, error) {
	token, err := jwtToken.NewJWTToken()
	if err != nil {
		logger.Error().Err(err).Msg("error creating token")
		return nil, err
	}
	return &MiddleWares{db: db, jwt: token, inMemDb: inMem, logger: logger}, nil
}

func (m *MiddleWares) CheckAuth(next http.Handler) http.Handler {
	return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		authHeader := r.Header.Get("Authorization")
		if authHeader == "" {
			m.logger.Warn().Msg("no authorization header")
			http.Error(w, "Отсутствует заголовок авторизации", http.StatusUnauthorized)
			return
		}

		// Ожидаем формат "Bearer {token}"
		const bearerPrefix = "Bearer "
		if len(authHeader) < len(bearerPrefix) || authHeader[:len(bearerPrefix)] != bearerPrefix {
			m.logger.Warn().Msg("invalid authorization header format")
			http.Error(w, "Неверный формат заголовка авторизации", http.StatusUnauthorized)
			return
		}

		token := authHeader[len(bearerPrefix):]
		if token == "" {
			m.logger.Warn().Msg("jwt token is empty")
			http.Error(w, "Пустой токен авторизации", http.StatusUnauthorized)
			return
		}
		email, err := m.jwt.ParseToken(token)
		switch {
		case errors.Is(err, jwt.ErrTokenExpired):
			m.logger.Warn().Str("email", email).Msg("jwt token has expired")
			http.Error(w, "Срок действия токена истек или он невалидный", http.StatusUnauthorized)
			return
		case err != nil:
			m.logger.Error().Err(err).Msg("failed to parse JWT token")
			http.Error(w, "Внутрення ошибка сервера", http.StatusInternalServerError)
			return
		default:
			m.logger.Info().Str("email", email).Msg("jwt token valid")
			exist, err := m.inMemDb.ExistJwt(r.Context(), email, token)
			if err != nil {
				m.logger.Error().Err(err).Msg("failed to check being jwt in in-memory db")
				http.Error(w, "Внутрення ошибка сервера", http.StatusInternalServerError)
				return
			}
			if !exist {
				m.logger.Warn().Str("email", email).Msg("jwt token not found in in-memory db")
				http.Error(w, "Срок действия токена истек или он невалидный", http.StatusUnauthorized)
				return
			}
			ctx := context.WithValue(r.Context(), "email", email)
			next.ServeHTTP(w, r.WithContext(ctx))
			return
		}
	})
}

func (m *MiddleWares) CheckAdmin(next http.Handler) http.Handler {
	return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		email, ok := r.Context().Value("email").(string)
		if !ok || email == "" {
			m.logger.Error().Msg("email is missing in context for admin check")
			http.Error(w, "Внутренняя ошибка сервера", http.StatusInternalServerError)
			return
		}

		isAdmin, err := m.db.IsAdmin(r.Context(), email)
		if err != nil {
			m.logger.Error().Err(err).Str("email", email).Msg("failed to check admin status")
			http.Error(w, "Внутренняя ошибка сервера", http.StatusInternalServerError)
			return
		}
		if !isAdmin {
			m.logger.Warn().Str("email", email).Msg("non-admin attempt to access admin route")
			http.Error(w, "Доступ запрещен", http.StatusForbidden)
			return
		}
		next.ServeHTTP(w, r)
	})
}
