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
	mqtt *mqtt.Client, passwordStore interfaces.PasswordKeeper, logger zerolog.Logger) {
	r := chi.NewRouter()
	h, err := handlers.NewHandler(db, sender, inMemDb, mqtt, passwordStore, logger)
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
			r.With(m.CheckAuth).Get("/me", h.GetMe)
			r.With(m.CheckAuth).Delete("/delete/user", h.DeleteUser)
			r.With(m.CheckAuth).Delete("/logout", h.Logout)
			r.With(m.CheckAuth).Post("/change/name", h.ChangeName)
			r.With(m.CheckAuth).Post("/fcm/update", h.UpdateFcmToken)
		})
		r.Route("/hive", func(r chi.Router) {
			r.Use(m.CheckAuth)
			r.Post("/create", h.CreateHive)
			r.Get("/list", h.GetHives)
			r.Get("/", h.GetHive)
			r.Put("/update", h.UpdateHive)
			r.Delete("/delete", h.DeleteHive)
			r.Post("/link/hub", h.LinkHubToHive)
			r.Post("/link/queen", h.LinkQueenToHive)
		})
		r.Route("/hub", func(r chi.Router) {
			r.Use(m.CheckAuth)
			r.Post("/create", h.CreateHub)
			r.Get("/list", h.GetHubs)
			r.Get("/", h.GetHub)
			r.Put("/update", h.UpdateHub)
			r.Delete("/delete", h.DeleteHub)
		})
		r.Route("/queen", func(r chi.Router) {
			r.Use(m.CheckAuth)
			r.Post("/create", h.CreateQueen)
			r.Get("/list", h.GetQueens)
			r.Get("/", h.GetQueen)
			r.Put("/update", h.UpdateQueen)
			r.Delete("/delete", h.DeleteQueen)
		})
		r.Route("/mqtt", func(r chi.Router) {
			r.Use(m.CheckAuth)
			r.Post("/config", h.MQTTSendConfig)
			r.Post("/health", h.MQTTSendHealthCheck)
			r.Get("/data", h.GetNoiseAndTemp)
		})
		r.Route("/telemetry", func(r chi.Router) {
			r.Use(m.CheckAuth)
			r.Get("/noise/get", h.GetNoiseSinceTime)
			r.Get("/weight/get", h.GetWeightSinceTime)
			r.Get("/temperature/get", h.GetTemperatureSinceTime)
			r.Get("/sensor/last", h.GetLastSensorReading)
			r.Post("/weight/set", h.SetHiveWeight)
			r.Delete("/weight/delete", h.DeleteHiveWeight)
		})
		r.Route("/task", func(r chi.Router) {
			r.Use(m.CheckAuth)
			r.Post("/create", h.CreateTask)
			r.Get("/list", h.GetTasks)
			r.Put("/update", h.UpdateTask)
			r.Delete("/delete", h.DeleteTask)
		})
	})

	srv := &http.Server{
		Addr:    serverPort,
		Handler: r,
	}

	quit := make(chan os.Signal, 1)
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
