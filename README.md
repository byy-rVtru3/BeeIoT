# 🐝 BeeIoT — PAC Repository

> Единый репозиторий программно-аппаратного комплекса (**PAC**): датчик в улье, серверная часть, мобильное приложение и веб-панель администратора контента.

BeeIoT — это система удалённого мониторинга пчелиных ульев. Автономный датчик внутри улья снимает температуру и уровень шума, передаёт телеметрию серверу по протоколу MQTT через NB-IoT, а пчеловод смотрит данные и получает push-уведомления в мобильном приложении.

[![Status](https://img.shields.io/badge/status-in_progress-yellow)](#)
[![Figma Board](https://img.shields.io/badge/Figma-Board-0ACF83?logo=figma&logoColor=white)](https://www.figma.com/board/7augfCx7bs5eItu9bLrrKX/BeeIoT?node-id=0-1&p=f&t=EtxzAufpNuN2hxLF-0)
[![Figma Design](https://img.shields.io/badge/Figma-Design-0ACF83?logo=figma&logoColor=white)](https://www.figma.com/design/XBaquza75M7RAq5aR2GChz/BeeIoT?node-id=0-1&p=f&t=Cz75UibSZ7Ut0IbE-0)

---

## Структура проекта

Проект состоит из четырёх компонентов:

1. **Backend** (`backend/`) — сервер на Go. Принимает MQTT-телеметрию от датчиков, предоставляет REST API мобильному приложению и веб-панели, фоном анализирует данные и шлёт push-уведомления через Firebase Cloud Messaging.
2. **Firmware** (`backend/firmware/beeiot_s3/`) — прошивка датчика на MicroPython для ESP32-S3 с NB-IoT-модемом SIM7020C, термодатчиком DS18B20 и I²S-микрофоном INMP441.
3. **Mobile App** (`frontend/Mobile/`) — Android-приложение пчеловода на Kotlin + Jetpack Compose.
4. **Web Admin** (`frontend/Web/`) — веб-панель администратора контента на React + TypeScript для управления экранами «О приложении» и «Как пользоваться».

```
┌─────────────────┐                ┌─────────────────────────────────────────┐
│ ESP32-S3 + …    │   MQTT/NB-IoT  │  Docker Compose                         │
│ DS18B20 + INMP441│ ─────────────▶│  nginx → go-server                      │
│ SIM7020C        │                │           ├─ postgres 16 (телеметрия)   │
└─────────────────┘                │           ├─ redis 7 (cache + JWT)      │
                                   │           ├─ mosquitto (MQTT-broker)    │
                                   │           └─ swagger-ui, web-admin      │
                                   └────────┬────────────────────────┬───────┘
                                            │ REST + JWT             │ FCM
                                       ┌────▼────┐              ┌────▼────┐
                                       │ Android │              │ Firebase│
                                       │ + Web   │              │  Push   │
                                       └─────────┘              └─────────┘
```

---

## Запуск и настройка

### 1. Сервер (Backend)

**Требования:** Docker, Docker Compose, Make, k6 (только для нагрузочных тестов).

**Шаги:**

1. Перейти в директорию `backend/`.
2. Создать файл `backend/.env` со следующими переменными (минимальный список — см. `build/docker-compose.yml` для полного):

   ```dotenv
   DB_HOST=db
   DB_PORT=5432
   DB_USER=...
   DB_PASSWORD=...
   DB_NAME=...
   REDIS_ADDR=redis:6379
   REDIS_PASSWORD=...
   REDIS_DB=0
   MQTT_HOST=mqtt
   MQTT_PORT=1883
   JWT_SECRET=...
   SMTP_HOST=...
   SMTP_PORT=587
   SMTP_USER=...
   SMTP_PASS=...
   FIREBASE_DATA=<base64-encoded service-account.json>
   ```

3. Поднять стек:

   ```bash
   make run_build
   ```

**Полезные Make-цели:**

| Цель | Что делает |
|---|---|
| `make run` / `make run_build` | Поднимает весь docker-compose-стек (с пересборкой во втором варианте) |
| `make stop` | Останавливает стек |
| `make logs` | Логи всех контейнеров |
| `make load_test` | Запускает k6-сценарий из `tests/load/load-test.js` (только для тестового профиля) |
| `make unit_test` | `go test -race ./...` с покрытием |
| `make admin EMAIL=foo@bar.com` | Сделать пользователя админом контента |
| `make unadmin EMAIL=foo@bar.com` | Снять админский флаг |
| `make list_admins` | Список текущих администраторов |
| `make notify_demo` | Демо-рассылка push-уведомлений всем пользователям по их ульям (для презентаций, не модифицирует БД) |

**REST API** (chi router, JWT HS256, TTL 30 дней, whitelist в Redis):

- `/api/auth/*` — регистрация, логин, refresh, FCM-токены
- `/api/hive/*` — CRUD ульев и привязки hub/queen
- `/api/hub/*` — CRUD датчиков-хабов
- `/api/queen/*` + `/api/calcQueen/calc` — матки и расчёт фаз развития
- `/api/telemetry/*` — temperature/noise/weight (история и ручной ввод массы)
- `/api/task/*` — журнал работ
- `/api/app-description`, `/api/instruction/items` — публичный контент для приложения
- `/api/admin/*` — приватные endpoint-ы для веб-панели администратора

**MQTT-топики:**

- `⬇ /device/{id}/data` — телеметрия (`temperature`, `noise`, `weight` — последний пока не используется прошивкой)
- `⬇ /device/{id}/status` — `battery_level`, `signal_strength`, `errors[]`
- `⬆ /device/{id}/config` — retained-сообщение с интервалами/командами

Все сообщения публикуются с QoS 1; на сервере идемпотентность гарантируется `UNIQUE(hub_id, recorded_at)` + `INSERT … ON CONFLICT DO NOTHING`.

### 2. Датчик (Firmware)

Актуальная прошивка — `backend/firmware/beeiot_s3/`. Реализована на MicroPython под ESP32-S3.

**Настройка:**

1. В `config.py` задать `DEVICE_ID`, адрес MQTT-брокера и параметры NB-IoT/Wi-Fi.
2. Прошить ESP32-S3 актуальным MicroPython.
3. Залить файлы из `beeiot_s3/` в корень файловой системы устройства.
4. Включить питание — устройство сразу пройдёт цикл `READ_SENSORS → CONNECT → SUBSCRIBE_CONFIG → PUBLISH_STATUS → WAIT_CONFIG → PUBLISH_DATA` и уйдёт в `deep sleep` до следующего интервала.

При отсутствии связи измерения буферизуются локально (RAM/SD/Flash, выбирается через `BUFFER_BACKEND`) и досылаются после восстановления соединения. Если сенсор не инициализировался или вернул ошибку — соответствующий тег уходит в `status.errors[]`.

### 3. Мобильное приложение (Android)

Реализация — `frontend/Mobile/` (Kotlin, Jetpack Compose, Material Design 3, MVI + Clean, Koin, Retrofit, Kotlinx Serialization, DataStore, Vico, ML Kit Code Scanner для QR, Firebase Messaging).

**Требования:** JDK 17+, Android Studio с поддержкой compileSdk 36, реальное устройство или эмулятор на API 26+.

**Сборка:**

1. Открыть `frontend/Mobile` в Android Studio.
2. Дождаться синхронизации Gradle.
3. Прописать `BASE_URL` сервера в `NetworkModule`.
4. Запустить приложение.

### 4. Веб-панель администратора

Реализация — `frontend/Web/` (React 19, TypeScript, Vite, Material UI, Zustand, React Query, Axios).

Поднимается автоматически в составе `docker compose` стек-а как сервис `web-admin` (контейнер на nginx, доступен на `:3000`). Логин разрешён только пользователям с флагом `is_admin = true` — назначается через `make admin EMAIL=...`.

Возможности администратора:

- CRUD инструкций экрана «Как пользоваться» (`/api/admin/instruction/items/*`).
- Редактирование текстов «О приложении» (`/api/admin/app-description`).

---

## Функциональность

* Регистрация и авторизация пчеловода с e-mail-подтверждением (двухшаговый код, JWT-аутентификация, bcrypt для паролей).
* Управление ульями: создание, редактирование, привязка датчика и матки, архивирование, восстановление, удаление.
* Управление датчиками-хабами: ручной ввод ID или сканирование QR-кода через Google ML Kit.
* Календарь матки: автоматический расчёт фаз развития по дате прививки.
* Графики телеметрии температуры, шума и массы за произвольный период (библиотека Vico).
* Ручной ввод массы улья за выбранную дату.
* Журнал работ по улью (создание, редактирование, удаление записей).
* Push-уведомления через Firebase Cloud Messaging:
  * аномальная температура (диапазон нормы 29–39 °C);
  * значимое изменение среднесуточного уровня шума;
  * слабый сигнал датчика;
  * сообщения об ошибках от прошивки;
  * дедупликация уведомлений на уровне сервера (одно уведомление одного типа в час по одному ульф/датчику).
* Веб-панель администрирования контента справки и экрана «О приложении».

---

## 🔗 Быстрые ссылки

- 📌 **Product Board** (задачи): https://www.figma.com/board/7augfCx7bs5eItu9bLrrKX/BeeIoT?node-id=0-1&p=f&t=EtxzAufpNuN2hxLF-0
- 🎨 **UI/UX Design** (макеты, компоненты): https://www.figma.com/design/XBaquza75M7RAq5aR2GChz/BeeIoT?node-id=0-1&p=f&t=Cz75UibSZ7Ut0IbE-0
- 🗂️ **Gantt chart**: https://miro.com/app/board/uXjVJsWVmWY=/

## 📁 Документы проекта

- 🗓️ **Project schedule**: https://docs.google.com/document/d/1yGd2L4erWQtEZQJV0UCVBrdDIWhEG6kKhHBEaxwgGOo/edit?usp=sharing
- 📄 **SRS**: https://docs.google.com/document/d/1QIodiUMq_Z5epvHOnq9zZvcVxb3OX3ylgJBSufI42yw/edit?usp=sharing
- 🎯 **Vision**: https://docs.google.com/document/d/1E6JWQt-eEOeX0CoDFyc3pHSL-0Htkao_hHzbLtlq5SY/edit?usp=sharing
- 📊 **Presentation**: https://docs.google.com/presentation/d/1yrYslXZLUU_pNW3oEP3yG8VLHuOIWepEDGlEcQZQyHI/edit?usp=sharing
- ⚠️ **Технические риски**: https://docs.google.com/document/d/1JEZ4LTdOlg_LpHXy_LAKT2Z9eAqTm-iWaqW3QQUOzyc/edit?usp=sharing
- 📄 **SAD**: https://docs.google.com/document/d/1KFIc90MHfPPeP4XmMzmz8R3hljP8pGWwcN34mUYic4I/edit?tab=t.0
