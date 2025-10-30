package http

import (
	"BeeIOT/internal/domain/interfaces"
	"BeeIOT/internal/http/handlers"
	"context"
	"errors"
	"log/slog"
	"net/http"
	"os"
	"os/signal"
	"syscall"
	"time"

	"github.com/go-chi/chi/v5"
	"github.com/go-chi/chi/v5/middleware"
)

func StartServer(db interfaces.DB, sender interfaces.ConfirmSender) {
	r := chi.NewRouter()
	h, err := handlers.NewHandler(db, sender)
	if err != nil {
		slog.Error("Failed to create handler", err)
		return
	}

	r.Use(middleware.Logger)
	r.Use(middleware.Recoverer)
	r.Use(middleware.Timeout(5 * time.Second))

	r.Post("/api/auth/registration", h.Registration)
	r.Post("api/auth/confirm/registration", h.ConfirmRegistration)
	r.Post("/api/auth/confirm/password", h.ConfirmChangePassword)
	r.Post("/api/auth/login", h.Login)
	r.Get("/api/auth/exist", h.ExistUser)

	srv := &http.Server{
		Addr:    ":8080",
		Handler: r,
	}

	quit := make(chan os.Signal)
	signal.Notify(quit, syscall.SIGINT, syscall.SIGTERM)
	go func() {
		// log
		if err := srv.ListenAndServe(); err != nil && !errors.Is(err, http.ErrServerClosed) {
			// fatal
		}
	}()
	<-quit
	// log
	ctx, cancel := context.WithTimeout(context.Background(), 5*time.Second)
	defer cancel()

	if err := srv.Shutdown(ctx); err != nil {
		// log
	}
	// log
}
