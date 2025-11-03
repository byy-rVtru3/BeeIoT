package http

import (
	"BeeIOT/internal/domain/interfaces"
	"BeeIOT/internal/http/handlers"
	"BeeIOT/internal/http/middlewares"
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

func StartServer(db interfaces.DB, sender interfaces.ConfirmSender, inMemDb interfaces.InMemoryDB) {
	r := chi.NewRouter()
	h, err := handlers.NewHandler(db, sender, inMemDb)
	if err != nil {
		slog.Error("Failed to create handler", err)
		return
	}
	m, err := middlewares.NewMiddleWares(inMemDb)
	if err != nil {
		slog.Error("Failed to create middlewares", err)
		return
	}

	r.Use(middleware.Logger)
	r.Use(middleware.Recoverer)
	r.Use(middleware.Timeout(5 * time.Second))

	r.Route("/api", func(r chi.Router) {
		r.Route("/auth", func(r chi.Router) {
			r.Post("/registration", h.Registration)
			r.Post("/login", h.Login)
			r.Post("/change", h.ChangePassword)
			r.Post("/confirm/registration", h.ConfirmRegistration)
			r.Post("/confirm/password", h.ConfirmChangePassword)
			r.With(m.CheckAuth).Delete("/delete/user", h.DeleteUser)
			r.With(m.CheckAuth).Delete("/logout", h.Logout)
		})
		r.Route("/calcQueen", func(r chi.Router) {
			r.Post("/calc", h.QueenCalculator)
		})
	})

	srv := &http.Server{
		Addr:    ":8000",
		Handler: r,
	}

	quit := make(chan os.Signal)
	signal.Notify(quit, syscall.SIGINT, syscall.SIGTERM)
	go func() {
		slog.Debug("Starting HTTP server on :8000")
		if err := srv.ListenAndServe(); err != nil && !errors.Is(err, http.ErrServerClosed) {
			slog.Error("Failed to start HTTP server", err)
		}
	}()
	<-quit
	slog.Debug("Shutting down HTTP server")
	ctx, cancel := context.WithTimeout(context.Background(), 5*time.Second)
	defer cancel()

	if err := srv.Shutdown(ctx); err != nil {
		slog.Error("Failed to shutdown HTTP server", err)
	}
	slog.Debug("Shutting down gracefully")
}
