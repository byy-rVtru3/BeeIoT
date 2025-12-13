package main

import (
	"BeeIOT/internal/analyzer/noise"
	"BeeIOT/internal/analyzer/temperature"
	"BeeIOT/internal/domain/mqtt"
	"BeeIOT/internal/http"
	"BeeIOT/internal/infrastructure/postgres"
	redis2 "BeeIOT/internal/infrastructure/redis"
	smtp2 "BeeIOT/internal/infrastructure/smtp"
	"context"
	"os"
	"time"

	"github.com/rs/zerolog"
)

func main() {
	logger := zerolog.New(os.Stdout).With().Timestamp().Caller().Logger()
	logger.Info().Msg("Starting application...")

	logger.Info().Msg("Initializing Postgres...")
	db, err := postgres.NewDB()
	if err != nil {
		logger.Error().Err(err).Msg("Failed to connect to the database")
		return
	}
	defer func() {
		_ = db.CloseDB()
	}()

	logger.Info().Msg("Initializing SMTP...")
	smtp, err := smtp2.NewSMTP()
	if err != nil {
		logger.Error().Err(err).Msg("Failed to connect to SMTP")
		return
	}

	logger.Info().Msg("Initializing Redis...")
	redis, err := redis2.NewRedis()
	if err != nil {
		logger.Error().Err(err).Msg("Failed to connect to redis")
		return
	}
	defer func() {
		_ = redis.Close()
	}()

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
	http.StartServer(db, smtp, redis, mqttServer, logger)
}
