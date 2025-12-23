package http

import (
	"BeeIOT/internal/domain/interfaces"
	"BeeIOT/internal/domain/mqtt"
	"BeeIOT/internal/http/handlers"
	"BeeIOT/internal/http/middlewares"
	"context"
	"errors"
	"net/http"
	"os"
	"os/signal"
	"syscall"
	"time"

	"github.com/go-chi/chi/v5"
	"github.com/go-chi/chi/v5/middleware"
	"github.com/rs/zerolog"
)

const serverPort = ":8000"

func StartServer(db interfaces.DB, sender interfaces.ConfirmSender, inMemDb interfaces.InMemoryDB,
	mqtt *mqtt.Client, logger zerolog.Logger) {
	r := chi.NewRouter()
	h, err := handlers.NewHandler(db, sender, inMemDb, mqtt, logger)
	if err != nil {
		logger.Error().Err(err).Msg("could not create new handler")
		return
	}
	m, err := middlewares.NewMiddleWares(inMemDb, logger)
	if err != nil {
		logger.Error().Err(err).Msg("could not create new middleware")
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
			r.Post("/refresh/token", h.RefreshToken)
			r.With(m.CheckAuth).Delete("/delete/user", h.DeleteUser)
			r.With(m.CheckAuth).Delete("/logout", h.Logout)
		})
		r.Route("/calcQueen", func(r chi.Router) {
			r.Post("/calc", h.QueenCalculator)
		})
		r.Route("/hive", func(r chi.Router) {
			r.Use(m.CheckAuth)
			r.Post("/create", h.CreateHive)
			r.Get("/list", h.GetHives)
			r.Get("/", h.GetHive)
			r.Put("/update", h.UpdateHive)
			r.Delete("/delete", h.DeleteHive)
		})
		r.Route("/mqtt", func(r chi.Router) {
			r.Post("/config", h.MQTTSendConfig)
			r.Get("/data", h.GetNoiseAndTemp)
		})
	})

	srv := &http.Server{
		Addr:    serverPort,
		Handler: r,
	}

	quit := make(chan os.Signal)
	signal.Notify(quit, syscall.SIGINT, syscall.SIGTERM)
	go func() {
		logger.Info().Str("port", serverPort).Msg("starting server")
		if err := srv.ListenAndServe(); err != nil && !errors.Is(err, http.ErrServerClosed) {
			logger.Error().Err(err).Msg("could not start server")
		}
	}()
	<-quit
	logger.Info().Msg("shutting down server")
	ctx, cancel := context.WithTimeout(context.Background(), 5*time.Second)
	defer cancel()

	if err := srv.Shutdown(ctx); err != nil {
		logger.Error().Err(err).Msg("could not shutdown server")
		return
	}
	logger.Info().Msg("server gracefully stopped")
}
