package main

import (
	"context"
	"fmt"
	"log"
	"math"
	"math/rand"
	"os"
	"time"

	"github.com/jackc/pgx/v5/pgxpool"
)

func main() {
	dsn := os.Getenv("DATABASE_URL")
	if dsn == "" {
		dsn = fmt.Sprintf("postgres://%s:%s@%s:%s/%s?sslmode=disable",
			envOrDefault("DB_USER", "postgres"),
			envOrDefault("DB_PASSWORD", "postgres"),
			envOrDefault("DB_HOST", "localhost"),
			envOrDefault("DB_PORT", "5432"),
			envOrDefault("DB_NAME", "beeiot"),
		)
	}

	hub := "sensor_001"
	if len(os.Args) > 1 {
		hub = os.Args[1]
	}

	ctx := context.Background()
	pool, err := pgxpool.New(ctx, dsn)
	if err != nil {
		log.Fatal("connect:", err)
	}
	defer pool.Close()

	// Находим hub_id по sensor name
	var hubID int
	err = pool.QueryRow(ctx, `SELECT id FROM hubs WHERE sensor = $1`, hub).Scan(&hubID)
	if err != nil {
		log.Fatalf("hub %q not found: %v", hub, err)
	}
	fmt.Printf("Hub %q → id=%d\n", hub, hubID)

	// Вчера 00:00 UTC
	now := time.Now().UTC()
	yesterday := time.Date(now.Year(), now.Month(), now.Day()-1, 0, 0, 0, 0, time.UTC)

	var countNoise, countTemp, countWeight int

	for i := 0; i < 24; i++ {
		ts := yesterday.Add(time.Duration(i) * time.Hour)

		// Температура: 18–25°C с синусоидой
		temp := 21.5 + 3.5*math.Sin(float64(i-6)*math.Pi/12) + rand.Float64()*0.5
		_, err = pool.Exec(ctx,
			`INSERT INTO temperature (hub_id, level, recorded_at) VALUES ($1, $2, $3) ON CONFLICT DO NOTHING`,
			hubID, round2(temp), ts)
		if err != nil {
			log.Printf("temp insert: %v", err)
		} else {
			countTemp++
		}

		// Шум: 25–45 дБ
		noise := 30.0 + 10*math.Sin(float64(i-12)*math.Pi/12) + rand.Float64()*3
		_, err = pool.Exec(ctx,
			`INSERT INTO noise (hub_id, level, recorded_at) VALUES ($1, $2, $3) ON CONFLICT DO NOTHING`,
			hubID, round2(noise), ts)
		if err != nil {
			log.Printf("noise insert: %v", err)
		} else {
			countNoise++
		}

		// Вес: 45–47 кг с медленным ростом
		weight := 45.0 + float64(i)*0.08 + rand.Float64()*0.3
		_, err = pool.Exec(ctx,
			`INSERT INTO weight (hub_id, level, recorded_at) VALUES ($1, $2, $3) ON CONFLICT DO NOTHING`,
			hubID, round2(weight), ts)
		if err != nil {
			log.Printf("weight insert: %v", err)
		} else {
			countWeight++
		}
	}

	fmt.Printf("Inserted: temp=%d, noise=%d, weight=%d (for %s)\n", countTemp, countNoise, countWeight, yesterday.Format("2006-01-02"))
}

func round2(f float64) float64 {
	return math.Round(f*100) / 100
}

func envOrDefault(key, def string) string {
	if v := os.Getenv(key); v != "" {
		return v
	}
	return def
}
