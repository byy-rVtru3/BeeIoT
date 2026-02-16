// main_load.go - версия main.go для нагрузочного тестирования
//
// Отличия от обычного main.go:
// 1. MockSMTP - не отправляет реальные email, всегда возвращает успех
// 2. MockPasswordKeeper - всегда сохраняет фиксированный код "123456" вместо случайно сгенерированного
//
// Это позволяет нагрузочным тестам использовать предсказуемый код подтверждения
// без необходимости настройки реального SMTP сервера.
//
// Использование:
//   go run cmd/server/main_load.go
//   или
//   go build -o main_load cmd/server/main_load.go && ./main_load

package main

import (
	"BeeIOT/internal/analyzer/noise"
	"BeeIOT/internal/analyzer/temperature"
	"BeeIOT/internal/domain/interfaces"
	"BeeIOT/internal/domain/mqtt"
	"BeeIOT/internal/http"
	"BeeIOT/internal/infrastructure/postgres"
	redis2 "BeeIOT/internal/infrastructure/redis"
	"context"
	"os"
	"time"

	"github.com/rs/zerolog"
)

// MockSMTP - простой мок для нагрузочного тестирования
// Не отправляет реальные email, просто возвращает успех
type MockSMTP struct{}

func (m *MockSMTP) SendConfirmationCode(toEmail, code string) error {
	// Для нагрузочных тестов не отправляем реальные письма
	return nil
}

// MockPasswordKeeper - мок для хранения кодов подтверждения
// Всегда сохраняет код "123456" вместо реального сгенерированного
type MockPasswordKeeper struct {
	realKeeper interfaces.PasswordKeeper
}

func (m *MockPasswordKeeper) AddCode(ctx context.Context, email, code, password string, timeLive time.Duration) error {
	// Всегда сохраняем код "123456" для нагрузочных тестов
	return m.realKeeper.AddCode(ctx, email, "123456", password, timeLive)
}
func (m *MockPasswordKeeper) GetPassword(ctx context.Context, email string) (string, string, error) {
	return m.realKeeper.GetPassword(ctx, email)
}
func main() {
	logger := zerolog.New(os.Stdout).With().Timestamp().Caller().Logger()
	logger.Info().Msg("Starting application in LOAD TEST mode...")
	logger.Warn().Msg("⚠️  Using MOCK SMTP and fixed confirmation code '123456' for load testing")
	logger.Info().Msg("Initializing Postgres...")
	db, err := postgres.NewDB()
	if err != nil {
		logger.Error().Err(err).Msg("Failed to connect to the database")
		return
	}
	defer func() {
		_ = db.CloseDB()
	}()
	logger.Info().Msg("Initializing Mock SMTP (load test mode)...")
	smtp := &MockSMTP{}
	logger.Info().Msg("Initializing Redis...")
	redis, err := redis2.NewRedis()
	if err != nil {
		logger.Error().Err(err).Msg("Failed to connect to redis")
		return
	}
	defer func() {
		_ = redis.Close()
	}()
	// Оборачиваем redis в MockPasswordKeeper для фиксированного кода
	mockPasswordKeeper := &MockPasswordKeeper{realKeeper: redis}
	logger.Info().Msg("Starting analyzers...")
	analyzersCtx, cancel := context.WithCancel(context.WithValue(context.Background(), "logger", logger))
	defer cancel()
	go temperature.NewAnalyzer(analyzersCtx, 24*60*time.Hour, db, redis).Start()
	go noise.NewAnalyzer(analyzersCtx, 24*60*time.Hour, db, redis).Start()
	logger.Info().Msg("Initializing MQTT...")
	mqttServer, err := mqtt.NewMQTTClient(db, redis, logger)
	if err != nil {
		logger.Error().Err(err).Msg("Failed to connect to mqtt server")
		return
	}
	defer mqttServer.Disconnect()
	logger.Info().Msg("Starting HTTP server...")
	// Передаем мок SMTP и мок PasswordKeeper
	http.StartServer(db, smtp, redis, mqttServer, mockPasswordKeeper, logger)
}
