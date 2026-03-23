---
description: Этап 2 — Разработчик: реализация прошивки BeeIoT на MicroPython
agent_role: Senior MicroPython Developer
depends_on:
  - 01_architect.md
  - architecture.md  # выходной артефакт архитектора
outputs:
  - boot.py
  - main.py
  - config.py
  - sensors/temperature.py
  - sensors/noise.py
  - network/sim800l.py
  - storage/ring_buffer.py
---

# Этап 2: Разработчик

## Роль
Senior MicroPython Developer.

## Задача
Реализовать полный рабочий код прошивки BeeIoT строго по архитектуре из `architecture.md` и ТЗ из `.agents/rules/task.md`.

## Входные данные
- `architecture.md` — спроектированная архитектура (выход Архитектора)
- `docs/sensor_communication.md` — JSON-форматы топиков MQTT
- `docs/old_config.py` — значения констант для переноса в config.py
- `.agents/rules/task.md` — Мастер-промпт с алгоритмом работы устройства

## Ограничения (ОБЯЗАТЕЛЬНО СОБЛЮДАТЬ)
- **Запрещено** копировать файлы из `docs/` в код прошивки
- **Запрещены** бесконечные циклы `while` без таймаутов
- **Запрещены** "магические числа" в коде — все в `config.py`
- **Запрещено** оставлять необработанные исключения при работе с железом и сетью
- При любой критической ошибке → `machine.deepsleep()`

---

## Шаги реализации

### 1. config.py — конфигурационный файл

Перенеси ВСЕ константы из `docs/old_config.py` в новый `config.py`. Структуру разбей на секции с комментариями:

```python
# === SENSOR IDENTITY ===
SENSOR_ID = "sensor_001"  # ИЗМЕНИТЬ перед прошивкой

# === MQTT ===
MQTT_BROKER = "84.237.53.140"
MQTT_PORT = 1883
MQTT_USER = ""
MQTT_PASSWORD = ""
MQTT_KEEPALIVE = 60
MQTT_CONNECT_TIMEOUT_MS = 10_000
MQTT_CONFIG_WAIT_MS = 5_000    # Ожидание /config топика

# === MQTT TOPICS ===
TOPIC_DATA    = "/device/{id}/data"
TOPIC_STATUS  = "/device/{id}/status"
TOPIC_CONFIG  = "/device/{id}/config"

# === SIM800L ===
SIM800L_UART_ID  = 1
SIM800L_TX_PIN   = 17
SIM800L_RX_PIN   = 18
SIM800L_RST_PIN  = 16
SIM800L_BAUDRATE = 9600
GPRS_TIMEOUT_MS  = 60_000   # 60 секунд ожидания сети
MAX_RETRY        = 3

# === APN ===
APN          = "internet.mst.ru"
APN_USER     = ""
APN_PASSWORD = ""

# === GPIO ===
DS18B20_PIN   = 4
KY038_ADC_PIN = 6

# === SENSORS CALIBRATION ===
DB_CALIBRATION = 75.0
ADC_MAX_VALUE  = 4095
ADC_VREF       = 3.3

# === STORAGE ===
BUFFER_FILE_PATH      = "/data/offline_buffer.jsonl"
BUFFER_MAX_SIZE_BYTES = 10_240   # 10 KB

# === DEEP SLEEP ===
DEEP_SLEEP_DURATION_MS = 60_000  # 60 секунд
```

### 2. sensors/temperature.py — датчик DS18B20

Используй встроенные модули `onewire` и `ds18x20`.

Требования:
- Конструктор принимает пин из `config.DS18B20_PIN`
- `read()` → запуск конвертации (`convert_temp()`), задержка 750мс, чтение, возврат `float` или `-1`
- Весь код в `try/except Exception`, при ошибке логировать и возвращать `-1`
- Не использовать блокирующий `time.sleep` дольше 1 секунды — использовать `utime.sleep_ms(750)`

### 3. sensors/noise.py — датчик KY-038

Используй `machine.ADC`.

Требования:
- Конструктор принимает пин из `config.KY038_ADC_PIN`
- `read()` → сделать 10 измерений ADC с интервалом 10мс, взять пиковое значение (peak-to-peak), конвертировать в дБ
- Конвертация: `volts = peak_raw / ADC_MAX * ADC_VREF`, `db = 20 * math.log10(volts + 1e-9) + DB_CALIBRATION`
- При делении на ноль или `math.domain error` → возвращать `-1`
- Весь код в `try/except`

### 4. storage/ring_buffer.py — кольцевой буфер

Требования:
- Файл в формате JSONL (одна запись на строку)
- `push(record: dict)`: сериализовать `ujson.dumps(record)`, добавить `\n`, дописать в файл
- После каждого `push` проверять `os.stat(filepath)[6]` (размер файла)
- Если размер > `BUFFER_MAX_SIZE_BYTES`: читать файл, отбросить первую строку, перезаписать оставшееся
- `pop_all()`: прочитать все строки, распарсить `ujson.loads`, удалить файл, вернуть список dict
- `is_empty()`: проверить наличие файла и что он не пуст
- Создавать директорию `/data/` если не существует (`os.mkdir`)

### 5. network/sim800l.py — драйвер SIM800L

Требования:
- Инициализация: `machine.UART(uart_id, baudrate, tx, rx)`
- `_send_at(cmd, expected, timeout_ms)`: отправить команду, читать ответ до `timeout_ms`, вернуть строку ответа или `None` если таймаут
- `power_on()`: отправить `AT`, ожидать `OK` с таймаутом 5000мс, затем ждать регистрации в сети (`AT+CREG?` возвращает `,1` или `,5`) с таймаутом `GPRS_TIMEOUT_MS`; если не дождались → `return False`
- `power_off()`: отправить `AT+CPOWD=1`, подождать 2000мс; если есть RST PIN → подтянуть GPIO RST; **никогда не бросать исключение**
- `connect_gprs()`: `AT+SAPBR=3,1,"APN"`, `AT+SAPBR=1,1`, `AT+SAPBR=2,1` для проверки IP
- `mqtt_connect(...)`: использовать AT-команды MQTT (`AT+CMQTTSTART`, `AT+CMQTTACCQ`, `AT+CMQTTCONNECT`) или базовый TCP+MQTT (выбери то, что поддерживает SIM800L с библиотекой umqtt)

> **Важное замечание:** SIM800L не поддерживает нативные MQTT AT-команды в базовой прошивке. Используй TCP-соединение через AT+CIPSTART и библиотеку `umqtt.simple` поверх UART-прокси, **или** реализуй прямую TCP-передачу MQTT-пакетов вручную через AT+CIPSEND. Выбери более простой и надёжный подход и задокументируй его.

- `mqtt_publish(topic, payload, qos=1)`: публикация с таймаутом `MQTT_CONNECT_TIMEOUT_MS`
- `mqtt_subscribe(topic)`: подписка
- `mqtt_wait_message(timeout_ms)`: неблокирующее ожидание входящего сообщения; вернуть `dict` или `None`
- `mqtt_disconnect()`: корректное отключение

### 6. main.py — конечный автомат

Реализуй цикл жизненного цикла устройства СТРОГО по алгоритму из ТЗ:

```
STATE: INIT
  → Инициализировать все модули из import
  → Перейти в READ_SENSORS

STATE: READ_SENSORS
  → temp = temperature.read()  # -1 при ошибке
  → noise = noise.read()       # -1 при ошибке
  → timestamp = utime.time()   # UNIX timestamp
  → Сформировать data_payload и status_payload по форматам из docs
  → Перейти в CONNECT_NETWORK

STATE: CONNECT_NETWORK
  → sim800l.power_on()
  → if not ok: ring_buffer.push(data_payload) → перейти в SLEEP
  → if not sim800l.connect_gprs(): ring_buffer.push(data_payload) → перейти в SLEEP
  → if not sim800l.mqtt_connect(...): ring_buffer.push(data_payload) → перейти в SLEEP
  → Перейти в PUBLISH

STATE: PUBLISH
  → if not ring_buffer.is_empty():
      for record in ring_buffer.pop_all():
          sim800l.mqtt_publish(TOPIC_DATA, ujson.dumps(record))
  → sim800l.mqtt_publish(TOPIC_DATA, ujson.dumps(data_payload))
  → sim800l.mqtt_publish(TOPIC_STATUS, ujson.dumps(status_payload))
  → Перейти в LISTEN_CONFIG

STATE: LISTEN_CONFIG
  → sim800l.mqtt_subscribe(TOPIC_CONFIG)
  → config_msg = sim800l.mqtt_wait_message(MQTT_CONFIG_WAIT_MS)
  → if config_msg: apply_config(config_msg) и сохранить в файл
  → Перейти в SLEEP

STATE: SLEEP (ВСЕГДА выполняется последним)
  → try: sim800l.power_off()
  → except: pass  # игнорировать ошибки выключения
  → machine.deepsleep(DEEP_SLEEP_DURATION_MS)
```

**Важно:** весь `main.py` обёрнут в глобальный `try/except Exception`, который при любой необработанной ошибке вызывает `power_off()` + `machine.deepsleep()`.

### 7. boot.py — минимальный файл

```python
# boot.py — выполняется при каждом старте ESP32
import gc
gc.collect()
# Не инициализировать железо здесь — это задача main.py
```

---

## JSON-форматы (строго по docs/sensor_communication.md)

**Топик `/device/{id}/data`:**
```json
{
  "temperature": 34.5,
  "temperature_time": 1710408287,
  "noise": 45.2,
  "noise_time": 1710408287
}
```

**Топик `/device/{id}/status`:**
```json
{
  "battery_level": 85,
  "signal_strength": 72,
  "timestamp": 1710408300,
  "errors": []
}
```

> `battery_level` и `signal_strength` = -1 если недоступны. `errors` — список строк.

---

## Критерии завершения
- [ ] Все 7 файлов созданы и синтаксически корректны для MicroPython
- [ ] Нет магических чисел вне `config.py`
- [ ] Все обращения к железу и сети в `try/except`
- [ ] `machine.deepsleep()` вызывается при любом сценарии завершения
- [ ] `sim800l.power_off()` всегда вызывается перед сном
- [ ] JSON-форматы строго соответствуют `docs/sensor_communication.md`
- [ ] Нет циклов `while` без условия выхода по таймауту
