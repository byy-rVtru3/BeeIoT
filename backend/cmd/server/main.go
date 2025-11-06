package main

import (
	"BeeIOT/internal/http"
	"BeeIOT/internal/infrastructure/postgres"
	redis2 "BeeIOT/internal/infrastructure/redis"
	smtp2 "BeeIOT/internal/infrastructure/smtp"
	"log/slog"
	"os"
)

func init() {
	// JSON вывод, показываем Debug и выше
	handler := slog.NewJSONHandler(os.Stdout, &slog.HandlerOptions{
		Level: slog.LevelDebug,
	})
	logger := slog.New(handler)
	slog.SetDefault(logger)
}

func main() {
	db, err := postgres.NewDB()
	if err != nil {
		slog.Error("Failed to connect to the database",
			"module", "server",
			"function", "main",
			"error", err)
		return
	}
	defer db.CloseDB()
	smtp, err := smtp2.NewSMTP()
	if err != nil {
		slog.Error("Failed to initialize SMTP",
			"module", "server",
			"function", "main",
			"error", err)
		return
	}
	redis, err := redis2.NewRedis()
	if err != nil {
		slog.Error("Failed to initialize Redis",
			"module", "server",
			"function", "main",
			"error", err)
		return
	}
	defer redis.Close()
	http.StartServer(db, smtp, redis)
}
