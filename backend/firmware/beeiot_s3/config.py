# -*- coding: utf-8 -*-
"""
config.py — Конфигурация прошивки BeeIoT (ESP32-S3 + SIM7020C + DS18B20 + INMP441).

ИНСТРУКЦИЯ: перед прошивкой нового устройства поменять DEVICE_ID.
"""

# === Идентификатор датчика (используется в MQTT-топиках) ===
DEVICE_ID = "sensor_001"

# === MQTT брокер ===
MQTT_BROKER  = "84.237.53.140"
MQTT_PORT    = 1883
MQTT_USER    = ""
MQTT_PASSWORD = ""
MQTT_KEEPALIVE = 60

TOPIC_DATA   = "/device/{}/data"
TOPIC_STATUS = "/device/{}/status"
TOPIC_CONFIG = "/device/{}/config"

# === SIM7020C (NB-IoT) ===
MODEM_UART_ID  = 1
MODEM_TX_PIN   = 43      # ESP32-S3 → SIM7020 RXD
MODEM_RX_PIN   = 44      # ESP32-S3 ← SIM7020 TXD
MODEM_BAUDRATE = 115200

# Если плата с PWRKEY — указать GPIO. -1 = автозапуск по питанию.
MODEM_PWRKEY_PIN = -1

# APN оператора (поменять под симку)
APN          = "internet.mst.ru"   # МТС
APN_USER     = ""
APN_PASSWORD = ""

# === Таймауты модема ===
MODEM_AT_TIMEOUT_MS       = 3_000
MODEM_BOOT_TIMEOUT_MS     = 10_000
MODEM_REGISTER_TIMEOUT_MS = 90_000   # NB-IoT холодная регистрация может тянуться до минуты
MQTT_CONNECT_TIMEOUT_MS   = 15_000
MQTT_CONFIG_WAIT_MS       = 5_000

# === GPIO датчиков ===
DS18B20_PIN    = 4

INMP441_SCK_PIN = 17     # I2S BCLK
INMP441_WS_PIN  = 19     # I2S LRCLK / WS
INMP441_SD_PIN  = 21     # I2S DIN

# === DS18B20 ===
TEMP_CONVERT_MS = 750    # минимальное время конвертации по даташиту

# === INMP441 ===
I2S_ID             = 0
I2S_SAMPLE_RATE_HZ = 16_000
I2S_BITS           = 32          # INMP441 кладёт 24 бита данных в верхние биты 32-битного слова
I2S_BUFFER_BYTES   = 2_048
NOISE_WINDOW_MS    = 200         # окно для усреднения RMS
# Калибровка SPL: INMP441 даёт -26 dBFS при 94 dB SPL (1 кГц).
# Соответствует смещению ~120 при пересчёте dBFS → dB SPL.
NOISE_DB_OFFSET    = 120.0

# === Питание / батарея ===
# Делителя на ADC нет → шлём -1 (сервер интерпретирует как "нет данных").
BATTERY_LEVEL_DEFAULT = -1

# === Частоты по умолчанию (до получения /config от сервера) ===
DEFAULT_SAMPLING_NOISE   = 5
DEFAULT_SAMPLING_TEMP    = 5
DEFAULT_STATUS_FREQUENCY = 30

# === Локальный буфер на флеше (на случай отсутствия сети) ===
# Одна запись DeviceData в JSON-Lines ≈ 120 байт.
# 256 КБ ≈ 2200 записей. При sampling=5с → ~3 часа оффлайна.
# При sampling=60с → ~36 часов оффлайна.
# Можно увеличить до 1 МБ если позволяет флеш (большинство S3 плат имеют 4–8 МБ
# из которых под FAT доступно ~2–6 МБ).
BUFFER_FILE_PATH      = "/data/offline.jsonl"
BUFFER_MAX_SIZE_BYTES = 262_144    # 256 КБ

# === Deep sleep ===
# False → обычный sleep между циклами (USB остаётся доступен — удобно для отладки).
# True  → machine.deepsleep() (минимальное потребление, USB отваливается).
DEEP_SLEEP_ENABLED = False
DEEP_SLEEP_MS      = DEFAULT_SAMPLING_NOISE * 1_000

# === Отладка ===
DEBUG = True
