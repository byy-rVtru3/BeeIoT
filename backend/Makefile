.PHONY: run run_build load_test load_test_build unit_test client_test logs stop clean help admin unadmin list_admins notify_demo notify_demo_build

# Загрузка переменных окружения из .env
ifneq (,$(wildcard .env))
    include .env
endif

# Экспортировать все переменные из Makefile как переменные окружения
.EXPORT_ALL_VARIABLES:

# Переменные
CLIENT_CMD = go run cmd/client/main.go
BUILD_DIR = build
K6_TEST = tests/load/load-test.js
DOCKER_COMPOSE = docker compose -f $(BUILD_DIR)/docker-compose.yml
DOCKERFILE = $(BUILD_DIR)/Dockerfile
TEST_DOCKERFILE = $(BUILD_DIR)/TestDockerfile

# Цвета для вывода
GREEN = \033[0;32m
YELLOW = \033[0;33m
NC = \033[0m

help: ## Показать справку по командам
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf "  $(YELLOW)%-18s$(NC) %s\n", $$1, $$2}'

logs: ## Показать логи всех контейнеров
	@$(DOCKER_COMPOSE) logs -f

stop: ## Остановить все контейнеры
	@$(DOCKER_COMPOSE) down

run: ## Запустить сервер в Docker
	@$(DOCKER_COMPOSE) up -d
	@$(DOCKER_COMPOSE) logs -f go-server

run_build: ## Пересобрать и запустить сервер в Docker
	@$(DOCKER_COMPOSE) up -d --build go-server
	@$(DOCKER_COMPOSE) logs -f go-server

load_test: ## Запустить сервер в Docker и провести нагрузочный тест
	@DOCKERFILE=$(TEST_DOCKERFILE) $(DOCKER_COMPOSE) up -d
	@sleep 5
	@k6 run $(K6_TEST)

load_test_build: ## Пересобрать, запустить и провести нагрузочный тест
	@DOCKERFILE=$(TEST_DOCKERFILE) $(DOCKER_COMPOSE) up -d --build go-server
	@sleep 5
	@k6 run $(K6_TEST)

unit_test: ## Запустить unit тесты
	@go test -v -race -coverprofile=coverage.out ./...
	@go tool cover -func=coverage.out | tail -n 1

client_test: ## Запустить сервер в Docker и клиента локально
	@$(DOCKER_COMPOSE) up -d
	@sleep 5
	@DB_HOST=localhost REDIS_ADDR=localhost:6379 MQTT_HOST=localhost $(CLIENT_CMD)

clean: stop ## Очистить файлы и остановить инфраструктуру
	@rm -f coverage.out

admin: ## Сделать пользователя админом: make admin EMAIL=foo@bar.com
	@if [ -z "$(EMAIL)" ]; then echo "$(YELLOW)Используй: make admin EMAIL=foo@bar.com$(NC)"; exit 1; fi
	@$(DOCKER_COMPOSE) exec -T db psql -U $(DB_USER) -d $(DB_NAME) -c \
		"UPDATE users SET is_admin = true WHERE email = '$(EMAIL)' RETURNING email, is_admin;"

unadmin: ## Снять админку с пользователя: make unadmin EMAIL=foo@bar.com
	@if [ -z "$(EMAIL)" ]; then echo "$(YELLOW)Используй: make unadmin EMAIL=foo@bar.com$(NC)"; exit 1; fi
	@$(DOCKER_COMPOSE) exec -T db psql -U $(DB_USER) -d $(DB_NAME) -c \
		"UPDATE users SET is_admin = false WHERE email = '$(EMAIL)' RETURNING email, is_admin;"

list_admins: ## Показать всех админов
	@$(DOCKER_COMPOSE) exec -T db psql -U $(DB_USER) -d $(DB_NAME) -c \
		"SELECT email, name FROM users WHERE is_admin = true;"

notify_demo_build: ## Пересобрать образ notify_demo (запускать после изменений в коде)
	@echo "$(GREEN)==>$(NC) Building notify_demo image..."
	@docker build -f $(BUILD_DIR)/NotifyDockerfile -t beeiot-notify-demo .

notify_demo: ## Демо-рассылка push-уведомлений (использует уже собранный образ, см. notify_demo_build)
	@if ! docker image inspect beeiot-notify-demo >/dev/null 2>&1; then \
		echo "$(YELLOW)Образ beeiot-notify-demo не найден. Запусти: make notify_demo_build$(NC)"; exit 1; \
	fi
	@echo "$(GREEN)==>$(NC) Running notify_demo against the live stack..."
	@docker run --rm --network build_app-net --env-file .env beeiot-notify-demo

all: run ## Запустить все
