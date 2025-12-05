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

	// init postgres
	db, err := postgres.NewDB()
	if err != nil {
		logger.Error().Err(err).Msg("Failed to connect to the database")
		return
	}
	defer func() {
		_ = db.CloseDB()
	}()

	// init smtp
	smtp, err := smtp2.NewSMTP()
	if err != nil {
		logger.Error().Err(err).Msg("Failed to connect to SMTP")
		return
	}

	// init redis
	redis, err := redis2.NewRedis()
	if err != nil {
		logger.Error().Err(err).Msg("Failed to connect to redis")
		return
	}
	defer func() {
		_ = redis.Close()
	}()

	// start analyzers
	analyzersCtx, cancel := context.WithCancel(context.WithValue(context.Background(), "log", logger))
	defer cancel()
	temperature.NewAnalyzer(analyzersCtx, 24*60*time.Hour, db, redis).Start()
	noise.NewAnalyzer(analyzersCtx, 24*60*time.Hour, db, redis).Start()

	// init mqtt server
	mqttServer, err := mqtt.NewMQTTClient(db, redis)
	if err != nil {
		logger.Error().Err(err).Msg("Failed to connect to mqtt server")
		return
	}
	defer mqttServer.Disconnect()

	// start http server
	http.StartServer(db, smtp, redis, logger)
}
