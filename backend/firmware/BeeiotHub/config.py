# -*- coding: utf-8 -*-
"""
Конфигурация BeeIoT датчика
Измените параметры под свои нужды
"""

# ===== MQTT Broker =====
# Используем публичный тестовый брокер для проверки
# MQTT_BROKER = "test.mosquitto.org"  # Публичный тестовый брокер
MQTT_BROKER = "84.237.53.140"  # Ваш брокер
MQTT_PORT = 1883
MQTT_USER = ""
MQTT_PASSWORD = ""

# ===== Идентификатор датчика =====
# Уникальный ID этого датчика (будет использоваться в топиках)
DEVICE_ID = "sensor_001"  # ИЗМЕНИТЕ для каждого датчика

# ===== SIM800L (GPRS) =====
APN = "internet.mst.ru"  # APN вашего оператора (МТС, Билайн, Мегафон и т.д.)
APN_USER = ""  # Обычно пусто
APN_PASSWORD = ""  # Обычно пусто

# ===== GPIO пины =====
# DS18B20 температурный датчик
DS18B20_PIN = 4

# KY-038 датчик шума
KY038_ANALOG_PIN = 6  # ADC1_CH5
KY038_DIGITAL_PIN = 7  # Опционально

# SIM800L UART
SIM800L_TX_PIN = 17  # TX ESP32 -> RX SIM800L
SIM800L_RX_PIN = 18  # RX ESP32 -> TX SIM800L
SIM800L_RST_PIN = 16  # Опционально
SIM800L_UART_ID = 1
SIM800L_BAUDRATE = 9600

# ===== Настройки по умолчанию =====
# Частота сбора данных (секунды)
DEFAULT_SAMPLING_NOISE = 60  # Раз в минуту
DEFAULT_SAMPLING_TEMP = 30  # Раз в 30 секунд
DEFAULT_STATUS_FREQUENCY = 300  # Раз в 5 минут

# ===== Калибровка датчиков =====
# KY-038: преобразование в дБ с использованием peak-to-peak метода
# Новый метод: 20 * log10(volts) + DB_CALIBRATION
DB_CALIBRATION = 75.0  # Калибровочное смещение (подобрано экспериментально)

# Старые параметры (для legacy метода, если нужно)
NOISE_MIN_DB = 40.0  # Минимальный уровень шума
NOISE_MAX_DB = 120.0  # Максимальный уровень шума
ADC_MAX_VALUE = 4095  # 12-bit ADC на ESP32

# ===== Батарея (если используете ADC для измерения) =====
# Если измеряете напряжение батареи через делитель
BATTERY_ADC_PIN = None  # Установите пин, если используете
BATTERY_MIN_VOLTAGE = 3.0  # Минимальное напряжение (0%)
BATTERY_MAX_VOLTAGE = 4.2  # Максимальное напряжение (100%)

# ===== Таймауты и повторы =====
MQTT_KEEPALIVE = 60
MQTT_CONNECT_TIMEOUT = 10  # Уменьшено до 10 секунд (было 50000!)
GPRS_CONNECT_TIMEOUT = 30
MAX_RETRY_ATTEMPTS = 3

# ===== Deep Sleep =====
DEEP_SLEEP_ENABLED = False  # Включить для экономии батареи
DEEP_SLEEP_DURATION = 60  # Секунды между пробуждениями

# ===== Отладка =====
DEBUG = True  # Выключите для production
SKIP_SIM800L = False  # True = работа БЕЗ SIM800L (только датчики), False = полный режим
TEST_MODE = False  # False = полный режим с MQTT, True = тестовый режим (только датчики)

# ===== WiFi (резервный канал связи) =====
USE_WIFI_FALLBACK = False  # True = использовать WiFi если SIM800L не работает
WIFI_SSID = "Redmi Note 12 Pro+ 5G"  # ИЗМЕНИТЕ! Имя WiFi сети
WIFI_PASSWORD = "12345678"  # ИЗМЕНИТЕ! Пароль WiFi
WIFI_TIMEOUT = 15  # Таймаут подключения к WiFi (секунды)

# ===== Приоритет каналов связи =====
# "gsm" - сначала пробовать GSM/GPRS, потом WiFi
# "wifi" - сначала пробовать WiFi, потом GSM/GPRS
COMMUNICATION_PRIORITY = "wifi"
