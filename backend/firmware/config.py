# -*- coding: utf-8 -*-
"""
Конфигурация BeeIoT датчика
Измените параметры под свои нужды
"""

# ===== MQTT Broker =====
MQTT_BROKER = "your-mqtt-broker.com"  # ИЗМЕНИТЕ НА СВОЙ АДРЕС
MQTT_PORT = 1883
MQTT_USER = "your_username"  # ИЗМЕНИТЕ
MQTT_PASSWORD = "your_password"  # ИЗМЕНИТЕ

# ===== Идентификатор датчика =====
# Уникальный ID этого датчика (будет использоваться в топиках)
DEVICE_ID = "sensor_001"  # ИЗМЕНИТЕ для каждого датчика

# ===== SIM800L (GPRS) =====
APN = "internet"  # APN вашего оператора (МТС, Билайн, Мегафон и т.д.)
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
# KY-038: преобразование ADC в дБ (примерная формула, нужна калибровка)
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
MQTT_CONNECT_TIMEOUT = 10
GPRS_CONNECT_TIMEOUT = 30
MAX_RETRY_ATTEMPTS = 3

# ===== Deep Sleep =====
DEEP_SLEEP_ENABLED = False  # Включить для экономии батареи
DEEP_SLEEP_DURATION = 60  # Секунды между пробуждениями

# ===== Отладка =====
DEBUG = True  # Выключите для production
