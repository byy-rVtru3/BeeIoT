# -*- coding: utf-8 -*-
"""
config.py — Конфигурация прошивки BeeIoT (ESP32-S3 + SIM7020C + DS18B20 + INMP441).

ИНСТРУКЦИЯ: перед прошивкой нового устройства поменять DEVICE_ID.
"""

# === Идентификатор датчика (используется в MQTT-топиках) ===
DEVICE_ID = "sensor_001"

# === MQTT брокер ===
MQTT_BROKER  = "62.109.16.63"
MQTT_PORT    = 1883
MQTT_USER    = ""
MQTT_PASSWORD = ""
MQTT_KEEPALIVE = 60

TOPIC_DATA   = "/device/{}/data"
TOPIC_STATUS = "/device/{}/status"
TOPIC_CONFIG = "/device/{}/config"

# === SIM7020C (NB-IoT) ===
# Если симки нет — выруби чтобы не ждать таймаут каждый цикл.
MODEM_ENABLED  = False
MODEM_UART_ID  = 1
MODEM_TX_PIN   = 44      # ESP32-S3 → SIM7020 RXD
MODEM_RX_PIN   = 2       # ESP32-S3 ← SIM7020 TXD
MODEM_BAUDRATE = 115200
# Инверсия UART не нужна — TXD/RXD идут напрямую без J3Y.
# Для совместимости с MicroPython передаём None если 0.
# Допустимые: UART.INV_TX=32, UART.INV_RX=4 или их сумма.
MODEM_UART_INVERT = None

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
INMP441_WS_PIN  = 18     # I2S LRCLK / WS
INMP441_SD_PIN  = 21     # I2S DIN

# === DS18B20 ===
TEMP_CONVERT_MS = 750    # минимальное время конвертации по даташиту

# === INMP441 ===
I2S_ID             = 0
# Минимум 32 кГц — иначе BCLK падает ниже 2.048 МГц и INMP441 шлёт мусор.
I2S_SAMPLE_RATE_HZ = 32_000
I2S_BITS           = 32          # INMP441 кладёт 24 бита данных в верхние биты 32-битного слова
I2S_BUFFER_BYTES   = 4_096
NOISE_WINDOW_MS    = 200         # окно для усреднения RMS
# Калибровка SPL. После фильтрации мусорных сэмплов RMS считается
# только по реальным значениям, поэтому offset близок к даташитовскому.
# Подкручивай под свою комнату: тихо (~40 dB) — занижай, шумно — наоборот.
NOISE_DB_OFFSET    = 100.0

# === Питание / батарея ===
# Делителя на ADC нет → шлём -1 (сервер интерпретирует как "нет данных").
BATTERY_LEVEL_DEFAULT = -1

# === Частоты ===
# Серверная конфа игнорируется — задаётся жёстко тут.
DEFAULT_SAMPLING_NOISE   = 3600   # 1 час
DEFAULT_SAMPLING_TEMP    = 3600
DEFAULT_STATUS_FREQUENCY = 3600
# Игнорить ли частоты от сервера (restart/delete всё равно применяются)
APPLY_SERVER_INTERVALS   = False

# === Локальный буфер ===
# Где хранить недослыннее данные при отказе сети:
#   "ram"   — только в RAM (пропадает при reset/power loss)  ← когда нет SD
#   "sd"    — SD-карта по SPI (нужна sdcard либа)
#   "flash" — внутренний флеш (FAT)
#   "auto"  — попытка SD, при неудаче → flash
BUFFER_BACKEND = "auto"   # SD если есть, иначе flash

# Лимиты на RAM-режим (по числу записей чтобы не съесть всю память)
BUFFER_RAM_MAX_RECS = 256

# Параметры файлового бэкенда (sd/flash/auto):
BUFFER_FILE_PATH       = "/sd/offline.jsonl"
BUFFER_FALLBACK_PATH   = "/data/offline.jsonl"
BUFFER_MAX_SIZE_BYTES  = 4_194_304   # 4 МБ — для SD; для flash советую снизить до 256_144
# Аккумулируем записи в RAM и сбрасываем на накопитель не чаще,
# чем раз в N мс или при накоплении N записей. Снижает износ FAT.
BUFFER_FLUSH_MIN_MS    = 30_000
BUFFER_FLUSH_MAX_RECS  = 16

# === SD-карта (модуль на SPI) — нужна только при BUFFER_BACKEND="sd"/"auto" ===
SD_SPI_ID    = 2          # HSPI на ESP32-S3
SD_SCK_PIN   = 12
SD_MOSI_PIN  = 11
SD_MISO_PIN  = 13
SD_CS_PIN    = 10
SD_MOUNT     = "/sd"

# === WiFi (резервный канал, если модем не смог) ===
WIFI_ENABLED  = True
WIFI_SSID     = "bee"
WIFI_PASSWORD = "bee12345678"
WIFI_CONNECT_TIMEOUT_MS = 15_000

# === Deep sleep ===
# False → обычный sleep между циклами (USB остаётся доступен — удобно для отладки).
# True  → machine.deepsleep() (минимальное потребление, USB отваливается).
DEEP_SLEEP_ENABLED = True
DEEP_SLEEP_MS      = DEFAULT_SAMPLING_NOISE * 1_000

# === Отладка ===
DEBUG = True
