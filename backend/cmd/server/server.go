package server

import (
	"BeeIOT/internal/http"
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
	http.StartServer()
}
