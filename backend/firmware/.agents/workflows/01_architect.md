---
description: Этап 1 — Архитектор: проектирование модульной архитектуры BeeIoT
agent_role: Архитектор (Systems Architect, MicroPython/ESP32)
depends_on: []
outputs:
  - architecture.md
---

# Этап 1: Архитектор

## Роль
Системный Архитектор встраиваемых систем (MicroPython, ESP32).

## Задача
Спроектировать полную модульную архитектуру прошивки BeeIoT на основе ТЗ (`docs/task.md`) и документации (`docs/sensor_communication.md`, `docs/old_config.py`).

## Входные данные
- `docs/sensor_communication.md` — MQTT-топики и JSON-форматы
- `docs/old_config.py` — существующие константы (пины, адреса, тайминги)
- `.agents/rules/task.md` — Мастер-промпт (полное ТЗ)

## Шаги выполнения

### 1. Изучи документацию
Прочитай файлы в папке `docs/`:
- Извлеки все пины GPIO (DS18B20, KY-038, SIM800L UART/RST)
- Извлеки адрес MQTT-брокера, порт, QoS
- Изучи структуры JSON для топиков `/data`, `/status`, `/config`

### 2. Спроектируй структуру файлов проекта
Обязательная файловая структура:

```
firmware/
├── boot.py          # Минимальная инициализация при старте ESP32
├── main.py          # Точка входа, конечный автомат жизненного цикла
├── config.py        # ВСЕ константы: пины, APN, MQTT, тайминги, sensor_id
├── sensors/
│   ├── temperature.py   # Модуль DS18B20 (onewire + ds18x20)
│   └── noise.py         # Модуль KY-038 (ADC)
├── network/
│   └── sim800l.py       # Драйвер SIM800L (UART, AT-команды, GPRS, MQTT)
└── storage/
    └── ring_buffer.py   # Кольцевой буфер для flash-памяти
```

### 3. Опиши скелеты всех классов и их контракты

Для каждого модуля опиши:
- Имя класса и конструктор (`__init__`)
- Публичные методы с сигнатурами и описанием возвращаемых значений
- Исключения, которые могут быть выброшены
- Что НЕ входит в ответственность модуля (явная граница)

#### Обязательные классы:

**`config.py`** — не класс, только константы. Перечисли все секции:
- MQTT (брокер, порт, пользователь, пароль, keepalive, таймауты)
- Идентификатор датчика (SENSOR_ID)
- Пины GPIO (DS18B20_PIN, KY038_PIN, SIM800L_TX/RX/RST)
- APN настройки (APN, APN_USER, APN_PASSWORD)
- Тайминги (DEEP_SLEEP_DURATION, GPRS_TIMEOUT, MQTT_CONFIG_WAIT, MAX_RETRY)
- Локальное хранилище (BUFFER_FILE_PATH, BUFFER_MAX_SIZE_BYTES)
- Параметры датчиков (ADC_MAX, DB_CALIBRATION)

**`TemperatureSensor`** (sensors/temperature.py):
- `__init__(pin)` — инициализация 1-Wire шины
- `read() -> float` — возвращает температуру °C или `-1` при ошибке
- Внутри всегда `try/except`, никогда не бросает наружу

**`NoiseSensor`** (sensors/noise.py):
- `__init__(pin)` — инициализация ADC
- `read() -> float` — возвращает уровень шума в дБ или `-1` при ошибке
- Метод конвертации ADC → dB через формулу `20 * log10(volts) + DB_CALIBRATION`

**`SIM800L`** (network/sim800l.py):
- `__init__(uart_id, tx, rx, rst)` — инициализация UART
- `power_on() -> bool` — включение, ожидание регистрации в сети (таймаут из config)
- `power_off() -> None` — AT+CPOWD=1 или GPIO RST, гарантированное выключение
- `connect_gprs() -> bool` — поднять GPRS через APN
- `mqtt_connect(broker, port, client_id, user, password) -> bool`
- `mqtt_publish(topic, payload, qos=1) -> bool`
- `mqtt_subscribe(topic) -> None`
- `mqtt_wait_message(timeout_ms) -> dict | None` — ожидание с таймаутом, возвращает распарсенный JSON или None
- `mqtt_disconnect() -> None`
- `_send_at(cmd, expected, timeout_ms) -> str | None` — внутренний метод AT-команд

**`RingBuffer`** (storage/ring_buffer.py):
- `__init__(filepath, max_size_bytes)` — путь к файлу и лимит
- `push(record: dict) -> None` — добавить запись; если превышен лимит, удалить самую старую
- `pop_all() -> list[dict]` — прочитать и удалить все записи (для отправки при восстановлении сети)
- `is_empty() -> bool`
- `_trim_to_limit() -> None` — логика удаления старых записей (перезапись файла без первой строки)

**`main.py`** — конечный автомат (FSM):
- Состояния: INIT → READ_SENSORS → CONNECT_NETWORK → PUBLISH → LISTEN_CONFIG → SLEEP
- Переход в SLEEP при любой критической ошибке
- Финальный шаг ВСЕГДА вызывает `sim800l.power_off()` и `machine.deepsleep()`

### 4. Опиши логику кольцевого буфера детально
- Формат файла: одна JSON-запись на строку (JSONL)
- `push`: сериализовать dict в JSON, добавить `\n`, дописать в файл
- `_trim_to_limit`: после каждого `push` проверять `os.stat(file)[6]` (размер); если > MAX_SIZE, читать файл, отбросить первую строку (самую старую), перезаписать
- `pop_all`: прочитать все строки, распарсить, удалить файл или обнулить его

### 5. Выходной артефакт
Сохрани результат в файл `architecture.md` рядом с проектом. Пиши простым текстом, только скелеты классов в виде Python-псевдокода и текстовые описания. Без реализации методов.

## Критерии завершения
- [ ] Все файлы и модули перечислены с описанием назначения
- [ ] Все классы описаны с методами и сигнатурами
- [ ] Логика кольцевого буфера описана пошагово
- [ ] Конечный автомат в main.py описан с состояниями и переходами
- [ ] Создан файл `architecture.md`
