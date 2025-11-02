package middlewares

import (
	"BeeIOT/internal/domain/interfaces"
	"BeeIOT/internal/domain/jwtToken"
	"context"
	"errors"
	"log/slog"
	"net/http"

	"github.com/golang-jwt/jwt/v5"
)

type MiddleWares struct {
	inMemDb interfaces.InMemoryDB
	jwt     *jwtToken.JWTToken
}

func NewMiddleWares(inMem interfaces.InMemoryDB) (*MiddleWares, error) {
	token, err := jwtToken.NewJWTToken()
	if err != nil {
		slog.Error("Failed to create JWTToken instance",
			"module", "middlewares",
			"function", "NewMiddleWares",
			"error", err)
		return nil, err
	}
	return &MiddleWares{jwt: token, inMemDb: inMem}, nil
}

func (m *MiddleWares) CheckAuth(next http.Handler) http.Handler {
	return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		authHeader := r.Header.Get("Authorization")
		if authHeader == "" {
			slog.Warn("Authorization header is missing",
				"module", "middlewares",
				"function", "CheckAuth")
			http.Error(w, "Отсутствует заголовок авторизации", http.StatusUnauthorized)
			return
		}

		// Ожидаем формат "Bearer {token}"
		const bearerPrefix = "Bearer "
		if len(authHeader) < len(bearerPrefix) || authHeader[:len(bearerPrefix)] != bearerPrefix {
			slog.Warn("Invalid authorization header format",
				"module", "middlewares",
				"function", "CheckAuth")
			http.Error(w, "Неверный формат заголовка авторизации", http.StatusUnauthorized)
			return
		}

		token := authHeader[len(bearerPrefix):]
		if token == "" {
			slog.Warn("JWT token is empty",
				"module", "middlewares",
				"function", "CheckAuth")
			http.Error(w, "Пустой токен авторизации", http.StatusUnauthorized)
			return
		}
		email, err := m.jwt.ParseToken(token)
		switch {
		case errors.Is(err, jwt.ErrTokenExpired):
			slog.Warn("JWT token has expired",
				"module", "middlewares",
				"function", "CheckAuth",
				"email", email)
			http.Error(w, "Срок действия токена истек или он невалидный", http.StatusUnauthorized)
			return
		case err != nil:
			slog.Error("Failed to parse JWT token",
				"module", "middlewares",
				"function", "CheckAuth",
				"error", err)
			http.Error(w, "Внутрення ошибка сервера", http.StatusInternalServerError)
			return
		default:
			slog.Info("JWT token is valid",
				"module", "middlewares",
				"function", "CheckAuth",
				"email", email)
			exist, err := m.inMemDb.ExistJwt(r.Context(), email, token)
			if err != nil {
				slog.Error("Failed to check JWT existence in in-memory DB",
					"module", "middlewares",
					"function", "CheckAuth",
					"error", err)
				http.Error(w, "Внутрення ошибка сервера", http.StatusInternalServerError)
				return
			}
			if !exist {
				slog.Warn("JWT token not found in in-memory DB",
					"module", "middlewares",
					"function", "CheckAuth",
					"email", email)
				http.Error(w, "Срок действия токена истек или он невалидный", http.StatusUnauthorized)
				return
			}
			ctx := context.WithValue(r.Context(), "email", email)
			next.ServeHTTP(w, r.WithContext(ctx))
			return
		}
	})
}
