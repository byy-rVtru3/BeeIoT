// notify_demo — одноразовый скрипт, рассылающий тестовое push-уведомление
// «Критический уровень температуры в улье» каждому пользователю по каждому
// его улью, у которого привязан датчик-хаб.
//
// Используется только для демонстрации работы FCM на защите. Никаких записей
// в БД не делает — только читает: список ульев + последние показания датчиков
// из Redis + FCM-токены пользователей. Title, Body, Data, Important — один в
// один совпадают с тем, что шлёт боевой analyzer/temperature, поэтому на
// клиенте уведомление выглядит как настоящее.
package main

import (
	"BeeIOT/internal/domain/models/mqttTypes"
	"BeeIOT/internal/domain/notification"
	"BeeIOT/internal/infrastructure/postgres"
	redis2 "BeeIOT/internal/infrastructure/redis"
	"context"
	"encoding/json"
	"fmt"
	"os"
	"time"

	"github.com/rs/zerolog"
)

// Те же константы, что и в internal/analyzer/temperature/temperature.go —
// чтобы тело уведомления совпадало с реальным анализатором.
const (
	temperatureNormal  = 34.0
	temperatureDeltaUp = 5.0
)

func main() {
	logger := zerolog.New(os.Stdout).With().Timestamp().Caller().Logger()
	logger.Info().Msg("notify_demo: starting demo critical-temperature broadcast")

	ctx, cancel := context.WithTimeout(
		context.WithValue(context.Background(), "logger", logger),
		2*time.Minute,
	)
	defer cancel()

	db, err := postgres.NewDB()
	if err != nil {
		logger.Fatal().Err(err).Msg("failed to connect postgres")
	}
	defer db.CloseDB()

	rds, err := redis2.NewRedis()
	if err != nil {
		logger.Fatal().Err(err).Msg("failed to connect redis")
	}
	defer func() { _ = rds.Close() }()

	notify, err := notification.NewNotification(ctx, logger)
	if err != nil {
		logger.Fatal().Err(err).Msg("failed to init firebase notification client")
	}

	hives, err := db.GetHives(ctx, "", nil)
	if err != nil {
		logger.Fatal().Err(err).Msg("failed to list hives")
	}
	logger.Info().Int("total_hives", len(hives)).Msg("hives loaded")

	var sent, skipped, failed int
	for _, hive := range hives {
		log := logger.With().
			Int("hiveId", hive.Id).
			Str("hive", hive.NameHive).
			Str("email", hive.Email).
			Logger()

		// Ульи без привязанного хаба пропускаем — точно так же, как
		// поступает боевой анализатор температуры.
		if hive.HubID == nil || hive.HubName == "" {
			log.Debug().Msg("skip: no hub linked")
			skipped++
			continue
		}

		// Достаём последнее показание из Redis. HubName здесь — это
		// поле hubs.sensor, оно же device_id, по которому прошивка
		// публикует MQTT-сообщения, и оно же ключ кэша sensor_data:<id>.
		raw, err := rds.GetLastSensorData(ctx, hive.HubName)
		if err != nil {
			log.Warn().Err(err).Str("sensor", hive.HubName).Msg("skip: no sensor data in redis")
			skipped++
			continue
		}
		var d mqttTypes.DeviceData
		if err := json.Unmarshal([]byte(raw), &d); err != nil {
			log.Warn().Err(err).Msg("skip: malformed sensor data in redis")
			skipped++
			continue
		}

		// Прошивка шлёт -1 если датчик температуры не работает.
		// На демке такие ульи пропускаем — иначе в push улетит -1.00 °C
		// и выглядеть будет глупо.
		if d.Temperature == -1 {
			log.Warn().Float64("temp", d.Temperature).Msg("skip: temperature reading is -1 (sensor error)")
			skipped++
			continue
		}

		tokens, err := db.GetFirebaseToken(ctx, hive.Email)
		if err != nil {
			log.Warn().Err(err).Msg("skip: failed to load fcm tokens")
			skipped++
			continue
		}
		if len(tokens) == 0 {
			log.Warn().Msg("skip: user has no fcm tokens registered")
			skipped++
			continue
		}

		log.Info().Int("tokens", len(tokens)).Float64("temp", d.Temperature).Msg("sending demo notification")

		// Title / Body / Data / Important — побайтово совпадают с
		// internal/analyzer/temperature/temperature.go. Для демки берём
		// abnormalCount = 1 (одно реальное аномальное показание из Redis).
		_, sendErr := notify.SendNotification(ctx, notification.Data{
			Title: "Критический уровень температуры в улье",
			Body: fmt.Sprintf(`Последнее значение: %.2f (аномальных замеров: %d).
Норма: %.2f +- %.2f. Необходимо проверить состояние улья`,
				d.Temperature, 1, temperatureNormal, temperatureDeltaUp),
			Data: map[string]string{
				"hive": hive.NameHive,
			},
			Tokens:    tokens,
			Important: false,
		})
		// В отличие от боевого анализатора инвалидные токены НЕ удаляем
		// (по ТЗ — никаких изменений в БД). Просто логируем.
		if sendErr != nil {
			log.Warn().Err(sendErr).Msg("FCM send error (no DB mutation will be made)")
			failed++
			continue
		}
		sent++
	}

	logger.Info().
		Int("sent", sent).
		Int("skipped", skipped).
		Int("failed", failed).
		Int("total_hives", len(hives)).
		Msg("notify_demo: done")
}
