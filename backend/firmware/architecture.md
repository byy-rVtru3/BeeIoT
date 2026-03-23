# BeeIoT Firmware — Architecture Document

## Обзор проекта

Прошивка для IoT-датчика на ESP32 на MicroPython. Устройство просыпается по таймеру из Deep Sleep, читает данные с датчиков, передаёт их на MQTT-брокер через GSM (SIM800L), применяет полученную конфигурацию, выключает GSM-модуль и уходит в Deep Sleep.

---

## Структура файлов

```
firmware/
├── boot.py                  # Выполняется при старте ESP32, минимальная инициализация
├── main.py                  # Точка входа, конечный автомат (FSM)
├── config.py                # ВСЕ константы (пины, URL, тайминги, ID датчика)
├── sensors/
│   ├── __init__.py          # Пустой
│   ├── temperature.py       # Драйвер DS18B20 (onewire + ds18x20)
│   └── noise.py             # Драйвер KY-038 (ADC → dB)
├── network/
│   ├── __init__.py          # Пустой
│   └── sim800l.py           # Драйвер SIM800L (UART, AT-команды, GPRS, MQTT)
└── storage/
    ├── __init__.py          # Пустой
    └── ring_buffer.py       # Кольцевой буфер на flash (JSONL формат)
```

---

## config.py — секции констант

```python
# === SENSOR IDENTITY ===
SENSOR_ID = "sensor_001"          # Заменяется вручную перед прошивкой

# === MQTT ===
MQTT_BROKER = "84.237.53.140"
MQTT_PORT = 1883
MQTT_USER = ""
MQTT_PASSWORD = ""
MQTT_KEEPALIVE = 60
MQTT_CONNECT_TIMEOUT_MS = 10_000
MQTT_CONFIG_WAIT_MS = 5_000       # Время ожидания топика /config

# === MQTT TOPICS (шаблоны) ===
TOPIC_DATA   = "/device/{}/data"
TOPIC_STATUS = "/device/{}/status"
TOPIC_CONFIG = "/device/{}/config"

# === SIM800L ===
SIM800L_UART_ID  = 1
SIM800L_TX_PIN   = 17
SIM800L_RX_PIN   = 18
SIM800L_RST_PIN  = 16
SIM800L_BAUDRATE = 9600
SIM800L_POWER_ON_TIMEOUT_MS  = 5_000
SIM800L_POWER_OFF_WAIT_MS    = 2_000
GPRS_CONNECT_TIMEOUT_MS = 60_000

# === APN ===
APN          = "internet.mst.ru"
APN_USER     = ""
APN_PASSWORD = ""
MAX_RETRY    = 3

# === GPIO ===
DS18B20_PIN   = 4
KY038_ADC_PIN = 6

# === SENSORS ===
TEMP_MEASURE_WAIT_MS = 750     # Время конвертации DS18B20 (не менее 750мс)
NOISE_SAMPLES        = 10      # Кол-во измерений ADC для peak-to-peak
NOISE_SAMPLE_MS      = 10      # Интервал между измерениями ADC
DB_CALIBRATION       = 75.0
ADC_MAX_VALUE        = 4095
ADC_VREF             = 3.3

# === STORAGE ===
BUFFER_FILE_PATH      = "/data/offline.jsonl"
BUFFER_MAX_SIZE_BYTES = 10_240    # 10 KБ

# === DEEP SLEEP ===
DEEP_SLEEP_DURATION_MS = 60_000   # 60 секунд между пробуждениями
```

---

## Класс TemperatureSensor (sensors/temperature.py)

```python
import onewire, ds18x20, machine, utime

class TemperatureSensor:
    def __init__(self, pin: int):
        # Инициализирует 1-Wire шину на указанном пине
        # Находит первый DS18B20 на шине

    def read(self) -> float:
        # Запускает конвертацию температуры
        # Ждёт TEMP_MEASURE_WAIT_MS мс
        # Читает результат с датчика
        # Возвращает: float (°C) или -1 при любой ошибке
        # Никогда не выбрасывает исключение — всё внутри try/except
```

**Контракт:**
- Возвращает `float`: реальная температура или `-1.0` при любой ошибке
- Не блокирует поток дольше чем `TEMP_MEASURE_WAIT_MS + 50` мс
- Не управляет питанием, не логирует глобальное состояние

---

## Класс NoiseSensor (sensors/noise.py)

```python
import machine, utime, math

class NoiseSensor:
    def __init__(self, pin: int):
        # Инициализирует ADC на указанном пине
        # Настраивает ширину ADC (12 бит)

    def read(self) -> float:
        # Делает NOISE_SAMPLES измерений с интервалом NOISE_SAMPLE_MS мс
        # Вычисляет peak-to-peak (max - min) среди всех результатов
        # Конвертирует в dB: volts = peak / ADC_MAX * VREF
        #                     db = 20 * log10(volts + 1e-9) + DB_CALIBRATION
        # Возвращает: float (dB) или -1 при любой ошибке
        # Никогда не выбрасывает исключение

    def _adc_to_db(self, peak_raw: int) -> float:
        # Вычисляет уровень в dB из пикового значения ADC
```

**Контракт:**
- Возвращает `float`: уровень шума в dB или `-1.0` при любой ошибке
- Суммарное время блокировки: `NOISE_SAMPLES * NOISE_SAMPLE_MS` мс

---

## Класс RingBuffer (storage/ring_buffer.py)

```python
import ujson, os, utime

class RingBuffer:
    def __init__(self, filepath: str, max_size_bytes: int):
        # Сохраняет путь и лимит
        # Создаёт директорию если не существует (os.makedirs)

    def push(self, record: dict) -> None:
        # Сериализует record в JSON строку + '\n'
        # Дописывает в файл (режим 'a')
        # После записи: если os.stat(filepath)[6] > max_size_bytes → вызывает _trim()

    def pop_all(self) -> list:
        # Читает файл построчно
        # Парсит каждую строку через ujson.loads (плохие строки пропускает)
        # Удаляет файл (os.remove) или очищает его
        # Возвращает список dict (может быть пустым)

    def is_empty(self) -> bool:
        # Проверяет наличие файла и его размер > 0
        # Не выбрасывает исключение если файла нет

    def _trim(self) -> None:
        # Читает файл построчно, отбрасывает первую строку (самую старую)
        # Перезаписывает файл оставшимися данными
        # Если осталась одна строка и она не помещается — файл очищается
```

**Контракт:**
- Формат файла: JSONL (одна строка = один JSON-объект)
- При достижении лимита удаляет ОДНУ самую старую запись за раз
- Все методы обёрнуты в `try/except`, не бросают исключений наружу
- Оптимизация: `push` дописывает байты без чтения всего файла

---

## Класс SIM800L (network/sim800l.py)

```python
import machine, utime, ujson

class SIM800L:
    def __init__(self, uart_id: int, tx: int, rx: int, rst: int, baudrate: int):
        # Инициализирует UART
        # Сохраняет RST-пин как machine.Pin(rst, Pin.OUT)

    def _send_at(self, cmd: str, expected: str, timeout_ms: int) -> str | None:
        # Отправляет AT-команду через UART
        # Читает ответ по байтам до timeout_ms (используя utime.ticks_ms())
        # Возвращает строку ответа если 'expected' найден, иначе None

    def power_on(self) -> bool:
        # Отправляет AT, ждёт OK (SIM800L_POWER_ON_TIMEOUT_MS)
        # Затем polling AT+CREG? до ",1" или ",5" (сеть найдена)
        # Таймаут: GPRS_CONNECT_TIMEOUT_MS
        # Возвращает True если сеть найдена, False если таймаут

    def power_off(self) -> None:
        # Отправляет AT+CPOWD=1; ждёт SIM800L_POWER_OFF_WAIT_MS мс
        # Если RST-пин задан — подтягивает его
        # НИКОГДА не бросает исключение

    def connect_gprs(self) -> bool:
        # Последовательность AT-команд для поднятия GPRS:
        #   AT+SAPBR=3,1,"Contype","GPRS"
        #   AT+SAPBR=3,1,"APN","<APN>"
        #   AT+SAPBR=1,1  (открыть bearer)
        #   AT+SAPBR=2,1  (проверить IP)
        # Возвращает True если получен IP-адрес

    def mqtt_connect(self, broker: str, port: int, client_id: str,
                     user: str = "", password: str = "",
                     keepalive: int = 60) -> bool:
        # Открывает TCP-соединение через AT+CIPSTART
        # Формирует и отправляет MQTT CONNECT пакет вручную
        # Ждёт CONNACK с таймаутом MQTT_CONNECT_TIMEOUT_MS
        # Возвращает True при успехе

    def mqtt_publish(self, topic: str, payload: str, qos: int = 1) -> bool:
        # Формирует MQTT PUBLISH пакет (QoS 0 или 1)
        # Отправляет через AT+CIPSEND
        # Если QoS=1: ждёт PUBACK с таймаутом
        # Возвращает True при успехе

    def mqtt_subscribe(self, topic: str, qos: int = 1) -> bool:
        # Отправляет MQTT SUBSCRIBE пакет
        # Ждёт SUBACK с таймаутом MQTT_CONNECT_TIMEOUT_MS

    def mqtt_wait_msg(self, timeout_ms: int) -> dict | None:
        # Ждёт входящий PUBLISH пакет до timeout_ms
        # Если пришёл PINGREQ/другое — обрабатывает и продолжает ожидание
        # При получении PUBLISH: извлекает payload, парсит ujson.loads
        # Возвращает dict или None если таймаут или ошибка парсинга

    def mqtt_disconnect(self) -> None:
        # Отправляет MQTT DISCONNECT пакет
        # Закрывает TCP через AT+CIPCLOSE

    # Внутренние методы для MQTT-пакетов (encode/decode варинт длины и т.д.)
    def _mqtt_encode_remaining_length(self, length: int) -> bytes: ...
    def _mqtt_read_packet(self, timeout_ms: int) -> bytes | None: ...
```

**Контракт:**
- Все публичные методы возвращают `bool` или данные, никогда не бросают
- `power_off()` — гарантированный финальный вызов, без исключений
- Нет бесконечных циклов — все ожидания через `utime.ticks_diff()`

---

## main.py — Конечный автомат (FSM)

### Состояния

```
INIT → READ_SENSORS → CONNECT_NETWORK → PUBLISH → LISTEN_CONFIG → SLEEP
```

При любой ошибке на любом шаге → немедленно к `SLEEP`.

### Псевдокод

```python
import machine, utime, ujson
import config
from sensors.temperature import TemperatureSensor
from sensors.noise import NoiseSensor
from network.sim800l import SIM800L
from storage.ring_buffer import RingBuffer

def apply_config(cfg: dict) -> None:
    # Применяет пришедшую конфигурацию:
    # Обновляет config.DEFAULT_SAMPLING_NOISE, TEMP, STATUS если значение != -1
    # Если restart_device = True → machine.reset()
    # Если delete_device = True → стирает данные и переходит к deep sleep

def run():
    sim = SIM800L(...)
    buf = RingBuffer(config.BUFFER_FILE_PATH, config.BUFFER_MAX_SIZE_BYTES)
    
    try:
        # --- READ_SENSORS ---
        temp_sensor = TemperatureSensor(config.DS18B20_PIN)
        noise_sensor = NoiseSensor(config.KY038_ADC_PIN)
        now = utime.time()
        temperature = temp_sensor.read()      # float или -1
        noise = noise_sensor.read()           # float или -1
        
        data_payload = {
            "temperature": temperature,
            "temperature_time": now,
            "noise": noise,
            "noise_time": now
        }
        status_payload = {
            "battery_level": -1,
            "signal_strength": -1,
            "timestamp": now,
            "errors": []
        }

        # --- CONNECT_NETWORK ---
        if not sim.power_on():
            buf.push(data_payload)
            # → SLEEP
            return

        if not sim.connect_gprs():
            buf.push(data_payload)
            # → SLEEP
            return

        topic_data   = config.TOPIC_DATA.format(config.SENSOR_ID)
        topic_status = config.TOPIC_STATUS.format(config.SENSOR_ID)
        topic_cfg    = config.TOPIC_CONFIG.format(config.SENSOR_ID)

        if not sim.mqtt_connect(config.MQTT_BROKER, config.MQTT_PORT,
                                config.SENSOR_ID, ...):
            buf.push(data_payload)
            # → SLEEP
            return

        # --- PUBLISH ---
        # Сначала — накопленные офлайн данные
        if not buf.is_empty():
            for record in buf.pop_all():
                sim.mqtt_publish(topic_data, ujson.dumps(record))

        # Затем — текущие данные
        sim.mqtt_publish(topic_data,   ujson.dumps(data_payload))
        sim.mqtt_publish(topic_status, ujson.dumps(status_payload))

        # --- LISTEN_CONFIG ---
        sim.mqtt_subscribe(topic_cfg)
        cfg_msg = sim.mqtt_wait_msg(config.MQTT_CONFIG_WAIT_MS)
        if cfg_msg:
            apply_config(cfg_msg)

    except Exception as e:
        # Глобальный обработчик — любая необработанная ошибка
        pass  # Продолжаем к SLEEP

    finally:
        # --- SLEEP — всегда выполняется ---
        try:
            sim.power_off()
        except:
            pass
        machine.deepsleep(config.DEEP_SLEEP_DURATION_MS)

run()
```

### Гарантии финального блока `finally`
- `sim.power_off()` и `machine.deepsleep()` вызываются ВСЕГДА
- Даже при `machine.reset()` внутри `apply_config()` это нормально — устройство перезагрузится

---

## Логика кольцевого буфера

1. **Формат файла:** каждая запись — одна строка JSON, разделитель `\n` (JSONL)
2. **push:** `open(file, 'a')` → записать строку → закрыть → проверить размер
3. **trim:** если `os.stat(file)[6] > MAX_SIZE`:
   - Открыть файл для чтения, считать все строки
   - Удалить первую строку (самую старую запись)
   - Перезаписать файл оставшимися строками
   - Повторять пока размер не войдёт в лимит
4. **pop_all:** прочитать все строки → распарсить каждую → удалить файл → вернуть список
5. **Устойчивость:** битые строки при `pop_all` пропускаются (continue), не вызывают сбой
6. **RAM-эффективность:** trim читает файл полностью — для 10KB это допустимо на ESP32

---

## boot.py

```python
# boot.py — минимальный файл, запускается при каждом старте ESP32
import gc
gc.collect()
# Всё железо инициализируется в main.py
```
