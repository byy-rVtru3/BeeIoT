package client

import (
	"context"
	"fmt"
	"log"
	"net/http"
	"os"
	"os/exec"
	"time"
)

// ServerManager управляет запуском и остановкой тестового сервера
type ServerManager struct {
	cmd    *exec.Cmd
	cancel context.CancelFunc
}

// StartTestServer запускает сервер для тестирования
func StartTestServer() (*ServerManager, error) {
	// Проверяем доступность порта
	if !isPortAvailable("localhost:8080") {
		return nil, fmt.Errorf("port 8080 is already in use")
	}

	// Устанавливаем переменные окружения для тестового сервера
	env := os.Environ()
	env = append(env,
		"JWT_SECRET=test_secret_key_for_integration_tests",
		"DB_HOST=localhost",
		"DB_PORT=5432",
		"DB_NAME=beeiot_test",
		"DB_USER=postgres",
		"DB_PASSWORD=postgres",
		"REDIS_HOST=localhost:6379",
		"SMTP_HOST=smtp.example.com",
		"SMTP_PORT=587",
		"SMTP_USERNAME=test@example.com",
		"SMTP_PASSWORD=testpassword",
	)

	// Создаем контекст для управления сервером
	ctx, cancel := context.WithCancel(context.Background())

	// Запускаем сервер
	cmd := exec.CommandContext(ctx, "go", "run", "../server/main.go")
	cmd.Env = env
	cmd.Dir = "../../" // Переходим в корень проекта

	// Перенаправляем вывод для отладки
	cmd.Stdout = os.Stdout
	cmd.Stderr = os.Stderr

	if err := cmd.Start(); err != nil {
		cancel()
		return nil, fmt.Errorf("failed to start test server: %w", err)
	}

	// Ждем пока сервер запустится
	if err := waitForServer("http://localhost:8080", 30*time.Second); err != nil {
		cancel()
		cmd.Process.Kill()
		return nil, fmt.Errorf("test server failed to start: %w", err)
	}

	return &ServerManager{
		cmd:    cmd,
		cancel: cancel,
	}, nil
}

// Stop останавливает тестовый сервер
func (sm *ServerManager) Stop() error {
	if sm.cancel != nil {
		sm.cancel()
	}

	if sm.cmd != nil && sm.cmd.Process != nil {
		return sm.cmd.Process.Kill()
	}

	return nil
}

// isPortAvailable проверяет доступность порта
func isPortAvailable(address string) bool {
	conn, err := http.Get("http://" + address)
	if err != nil {
		return true // Порт свободен
	}
	conn.Body.Close()
	return false // Порт занят
}

// waitForServer ждет пока сервер станет доступен
func waitForServer(url string, timeout time.Duration) error {
	client := &http.Client{
		Timeout: 5 * time.Second,
	}

	deadline := time.Now().Add(timeout)

	for time.Now().Before(deadline) {
		resp, err := client.Get(url + "/health")
		if err == nil && resp.StatusCode == http.StatusOK {
			resp.Body.Close()
			log.Println("Test server is ready")
			return nil
		}
		if resp != nil {
			resp.Body.Close()
		}

		time.Sleep(500 * time.Millisecond)
	}

	return fmt.Errorf("server did not become available within %v", timeout)
}

// SetupTestDatabase подготавливает тестовую базу данных
func SetupTestDatabase() error {
	// Здесь можно добавить логику создания тестовой БД
	// Пока используем основную базу данных
	return nil
}

// CleanupTestDatabase очищает тестовую базу данных
func CleanupTestDatabase() error {
	// Здесь можно добавить логику очистки тестовой БД
	return nil
}
