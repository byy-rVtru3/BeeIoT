# -*- coding: utf-8 -*-
"""
BeeIoT - Прошивка для датчика улья на ESP32-S3
Отправка данных о температуре и шуме через MQTT по GPRS
"""

print("[BOOT] ===== System boot initiated =====")

import time
import ujson
import machine
import os
import sys
from config import *
from sim800l import SIM800L
from sensors import DS18B20Sensor, KY038Sensor, BatterySensor
from mqtt_client import SimpleMQTT
from wifi_manager import WiFiManager
from wifi_mqtt import WiFiMQTT

# Файл для сохранения конфигурации
CONFIG_FILE = "device_config.json"

print("[BOOT] ===== System boot completed =====\n")


def calculate_deep_sleep_time(sampling_temp, sampling_noise):
    """
    Рассчитывает оптимальное время глубокого сна для экономии энергии

    :param sampling_temp: Частота замера температуры в секундах (-1 если отключено)
    :param sampling_noise: Частота замера шума в секундах (-1 если отключено)
    :return: Время сна в миллисекундах или 0 если сон не нужен
    """
    # Если обе частоты отключены - спим минимально
    if sampling_temp == -1 and sampling_noise == -1:
        return 0

    # Находим минимальную частоту (максимальное время сна)
    active_frequencies = []
    if sampling_temp != -1:
        active_frequencies.append(sampling_temp)
    if sampling_noise != -1:
        active_frequencies.append(sampling_noise)

    if not active_frequencies:
        return 0

    # Спим 90% от минимальной частоты (оставляем время на работу)
    min_frequency = min(active_frequencies)
    sleep_time = int(min_frequency * 0.9 * 1000)  # В миллисекундах

    # Ограничения: мин 10 сек, макс 1 час
    if sleep_time < 10000:
        return 10000
    if sleep_time > 3600000:
        return 3600000

    return sleep_time


class BeeIoTDevice:
    """Основной класс управления датчиком"""

    def __init__(self):
        """Инициализация устройства"""
        print("\n" + "=" * 50)
        print("BeeIoT Device Starting...")
        print("=" * 50 + "\n")

        print("[INIT] Initializing BeeIoTDevice class...")

        # Датчики
        self.temp_sensor = None
        self.noise_sensor = None
        self.battery_sensor = None
        print("[INIT] Sensor variables initialized")

        # Связь
        self.sim800l = None
        self.mqtt = None
        self.wifi = None
        self.wifi_mqtt = None
        self.communication_method = None  # "gsm" или "wifi"
        print("[INIT] Communication variables initialized")

        # Конфигурация - АВТОНОМНЫЙ РЕЖИМ (без сервера)
        # Устанавливаем значения по умолчанию сразу
        self.sampling_noise = 5  # Каждые 5 секунд
        self.sampling_temp = 5  # Каждые 5 секунд
        self.status_frequency = 30  # Статус каждые 30 секунд
        self.restart_requested = False
        self.health_check_requested = False
        self.delete_requested = False
        print("[INIT] Configuration variables initialized (AUTONOMOUS MODE)")
        print(f"[INIT] Default sampling_temp = {self.sampling_temp}s")
        print(f"[INIT] Default sampling_noise = {self.sampling_noise}s")
        print(f"[INIT] Default status_frequency = {self.status_frequency}s")

        # Флаг инициализации от сервера - ВСЕГДА TRUE в автономном режиме
        self.config_received = True
        print("[INIT] Config received flag = True (autonomous mode)")

        # Таймеры
        self.last_temp_reading = 0
        self.last_noise_reading = 0
        self.last_status_send = 0
        print("[INIT] Timer variables initialized")

        # Ошибки
        self.errors = []

        # Флаг для немедленной отправки статуса при критических ошибках
        self.critical_error_detected = False

        # В автономном режиме НЕ загружаем конфигурацию с флеша
        print("[INIT] Autonomous mode - skipping config file loading")
        print("[INIT] BeeIoTDevice initialization complete\n")

    def init_sensors(self):
        """Инициализация всех датчиков"""
        print("[INIT] ===== Initializing sensors =====")
        print(f"[INIT] DS18B20_PIN = {DS18B20_PIN}")
        print(f"[INIT] KY038_ANALOG_PIN = {KY038_ANALOG_PIN}")
        print(f"[INIT] KY038_DIGITAL_PIN = {KY038_DIGITAL_PIN}")
        print(f"[INIT] BATTERY_ADC_PIN = {BATTERY_ADC_PIN}")

        try:
            print("[INIT] Attempting to initialize DS18B20 temperature sensor...")
            # DS18B20 температура
            self.temp_sensor = DS18B20Sensor(DS18B20_PIN)
            print("[INIT] [OK] DS18B20 initialized successfully")
        except Exception as e:
            self.errors.append(f"DS18B20 init error: {e}")
            print(f"[ERROR] [FAIL] DS18B20 initialization failed: {e}")
            sys.print_exception(e)

        try:
            print("[INIT] Attempting to initialize KY-038 noise sensor...")
            # KY-038 шум
            self.noise_sensor = KY038Sensor(
                KY038_ANALOG_PIN,
                KY038_DIGITAL_PIN,
                NOISE_MIN_DB,
                NOISE_MAX_DB
            )
            print("[INIT] [OK] KY-038 initialized successfully")
        except Exception as e:
            self.errors.append(f"KY-038 init error: {e}")
            print(f"[ERROR] [FAIL] KY-038 initialization failed: {e}")
            sys.print_exception(e)

        try:
            print("[INIT] Attempting to initialize battery sensor...")
            # Батарея
            self.battery_sensor = BatterySensor(
                BATTERY_ADC_PIN,
                BATTERY_MIN_VOLTAGE,
                BATTERY_MAX_VOLTAGE
            )
            print("[INIT] [OK] Battery sensor initialized successfully")
        except Exception as e:
            print(f"[WARNING] Battery sensor initialization failed: {e}")
            sys.print_exception(e)

        print("[INIT] Sensor initialization complete\n")

    def init_sim800l(self):
        """Инициализация SIM800L и подключение к GPRS"""
        print("[INIT] ===== Initializing SIM800L =====")
        print(f"[INIT] SIM800L_UART_ID = {SIM800L_UART_ID}")
        print(f"[INIT] SIM800L_TX_PIN = {SIM800L_TX_PIN}")
        print(f"[INIT] SIM800L_RX_PIN = {SIM800L_RX_PIN}")
        print(f"[INIT] SIM800L_BAUDRATE = {SIM800L_BAUDRATE}")
        print(f"[INIT] SIM800L_RST_PIN = {SIM800L_RST_PIN}")

        try:
            print("[INIT] Creating SIM800L instance...")
            self.sim800l = SIM800L(
                SIM800L_UART_ID,
                SIM800L_TX_PIN,
                SIM800L_RX_PIN,
                SIM800L_BAUDRATE,
                SIM800L_RST_PIN
            )
            self.sim800l.debug = DEBUG
            print("[INIT] [OK] SIM800L instance created")

            # Инициализация модуля
            print("[INIT] Starting SIM800L module initialization...")
            if not self.sim800l.init():
                self.errors.append("SIM800L init failed")
                print("[ERROR] [FAIL] SIM800L init() returned False")
                return False
            print("[INIT] [OK] SIM800L module initialized")

            # Подключение к GPRS
            print(f"[INIT] Connecting to GPRS network...")
            print(f"[INIT] APN = {APN}")
            print(f"[INIT] APN_USER = {APN_USER}")
            if not self.sim800l.connect_gprs(APN, APN_USER, APN_PASSWORD):
                self.errors.append("GPRS connection failed")
                print("[ERROR] [FAIL] GPRS connection failed")
                return False

            print("[INIT] [OK] SIM800L ready\n")
            return True

        except Exception as e:
            self.errors.append(f"SIM800L error: {e}")
            print(f"[ERROR] [FAIL] SIM800L initialization exception: {e}")
            sys.print_exception(e)
            return False

    def init_mqtt(self):
        """Инициализация MQTT клиента"""
        print("[INIT] ===== Initializing MQTT =====")
        print(f"[INIT] DEVICE_ID = {DEVICE_ID}")
        print(f"[INIT] MQTT_BROKER = {MQTT_BROKER}")
        print(f"[INIT] MQTT_PORT = {MQTT_PORT}")
        print(f"[INIT] MQTT_USER = {MQTT_USER}")
        print(f"[INIT] MQTT_KEEPALIVE = {MQTT_KEEPALIVE}")

        try:
            print("[INIT] Creating SimpleMQTT client instance...")
            self.mqtt = SimpleMQTT(
                self.sim800l,
                DEVICE_ID,
                MQTT_BROKER,
                MQTT_PORT,
                MQTT_USER,
                MQTT_PASSWORD,
                MQTT_KEEPALIVE
            )
            print("[INIT] [OK] MQTT client instance created")

            # Подключение к MQTT брокеру
            print("[INIT] Connecting to MQTT broker...")
            if not self.mqtt.connect():
                self.errors.append("MQTT connection failed")
                print("[ERROR] [FAIL] MQTT connect() returned False")
                return False
            print("[INIT] [OK] Connected to MQTT broker")

            # Подписка на топик конфигурации
            config_topic = f"/device/{DEVICE_ID}/config"
            print(f"[INIT] Subscribing to config topic: {config_topic}")
            if not self.mqtt.subscribe(config_topic, qos=1):
                print(f"[WARNING] Failed to subscribe to {config_topic}")
            else:
                print(f"[INIT] [OK] Subscribed to {config_topic}")

            print("[INIT] [OK] MQTT ready\n")
            return True

        except Exception as e:
            self.errors.append(f"MQTT error: {e}")
            print(f"[ERROR] [FAIL] MQTT initialization exception: {e}")
            sys.print_exception(e)
            return False

    def init_wifi(self):
        """Инициализация WiFi подключения"""
        print("[INIT] ===== Initializing WiFi =====")
        print(f"[INIT] WIFI_SSID = {WIFI_SSID}")
        print(f"[INIT] WIFI_TIMEOUT = {WIFI_TIMEOUT}s")

        try:
            print("[INIT] Creating WiFi manager instance...")
            self.wifi = WiFiManager(WIFI_SSID, WIFI_PASSWORD, WIFI_TIMEOUT)
            print("[INIT] [OK] WiFi manager created")

            # Подключение к WiFi
            print("[INIT] Connecting to WiFi network...")
            if not self.wifi.connect():
                self.errors.append("WiFi connection failed")
                print("[ERROR] [FAIL] WiFi connection failed")
                return False

            print("[INIT] [OK] WiFi connected")

            # Проверка силы сигнала
            signal = self.wifi.get_signal_strength()
            print(f"[INIT] WiFi signal strength: {signal}%")

            return True

        except Exception as e:
            self.errors.append(f"WiFi error: {e}")
            print(f"[ERROR] [FAIL] WiFi initialization exception: {e}")
            sys.print_exception(e)
            return False

    def init_wifi_mqtt(self):
        """Инициализация MQTT клиента через WiFi"""
        print("[INIT] ===== Initializing WiFi-MQTT =====")
        print(f"[INIT] DEVICE_ID = {DEVICE_ID}")
        print(f"[INIT] MQTT_BROKER = {MQTT_BROKER}")
        print(f"[INIT] MQTT_PORT = {MQTT_PORT}")

        try:
            print("[INIT] Creating WiFi-MQTT client instance...")
            self.wifi_mqtt = WiFiMQTT(
                DEVICE_ID,
                MQTT_BROKER,
                MQTT_PORT,
                MQTT_USER,
                MQTT_PASSWORD,
                MQTT_KEEPALIVE,
                MQTT_CONNECT_TIMEOUT  # Добавляем таймаут
            )
            print("[INIT] [OK] WiFi-MQTT client created")

            # Подключение к MQTT брокеру
            print("[INIT] Connecting to MQTT broker via WiFi...")
            if not self.wifi_mqtt.connect():
                self.errors.append("WiFi-MQTT connection failed")
                print("[ERROR] [FAIL] WiFi-MQTT connection failed")
                return False

            print("[INIT] [OK] Connected to MQTT broker via WiFi")

            # Подписка на топик конфигурации
            config_topic = f"/device/{DEVICE_ID}/config"
            print(f"[INIT] Subscribing to config topic: {config_topic}")
            if not self.wifi_mqtt.subscribe(config_topic, qos=1):
                print(f"[WARNING] Failed to subscribe to {config_topic}")
            else:
                print(f"[INIT] [OK] Subscribed to {config_topic}")

            print("[INIT] [OK] WiFi-MQTT ready\n")
            return True

        except Exception as e:
            self.errors.append(f"WiFi-MQTT error: {e}")
            print(f"[ERROR] [FAIL] WiFi-MQTT initialization exception: {e}")
            sys.print_exception(e)
            return False

    def init_communication(self):
        """Инициализация канала связи с приоритетом"""
        print("[COMM] ===== Initializing communication channel =====")
        print(f"[COMM] Priority: {COMMUNICATION_PRIORITY}")
        print(f"[COMM] WiFi fallback: {USE_WIFI_FALLBACK}")

        gsm_success = False
        wifi_success = False

        # Попытка инициализации по приоритету
        if COMMUNICATION_PRIORITY == "wifi":
            # Сначала WiFi
            print("[COMM] Trying WiFi first (priority)...")
            if self.init_wifi() and self.init_wifi_mqtt():
                wifi_success = True
                self.communication_method = "wifi"
                self.mqtt = self.wifi_mqtt  # Используем WiFi MQTT
                print("[COMM] [OK] WiFi communication channel established")
                return True

            # Если WiFi не удался и не включен fallback - пробуем GSM
            if not SKIP_SIM800L:
                print("[COMM] WiFi failed, trying GSM as fallback...")
                if self.init_sim800l() and self.init_mqtt():
                    gsm_success = True
                    self.communication_method = "gsm"
                    print("[COMM] [OK] GSM communication channel established (fallback)")
                    return True

        else:  # COMMUNICATION_PRIORITY == "gsm"
            # Сначала GSM
            if not SKIP_SIM800L:
                print("[COMM] Trying GSM first (priority)...")
                if self.init_sim800l() and self.init_mqtt():
                    gsm_success = True
                    self.communication_method = "gsm"
                    print("[COMM] [OK] GSM communication channel established")
                    return True

            # Если GSM не удался и включен fallback - пробуем WiFi
            if USE_WIFI_FALLBACK:
                print("[COMM] GSM failed, trying WiFi as fallback...")
                if self.init_wifi() and self.init_wifi_mqtt():
                    wifi_success = True
                    self.communication_method = "wifi"
                    self.mqtt = self.wifi_mqtt  # Используем WiFi MQTT
                    print("[COMM] [OK] WiFi communication channel established (fallback)")
                    return True

        # Если оба не удались
        print("[COMM] [FAIL] All communication channels failed")
        print(f"[COMM] GSM: {'OK' if gsm_success else 'FAIL'}")
        print(f"[COMM] WiFi: {'OK' if wifi_success else 'FAIL'}")
        return False

    def read_and_publish_data(self):
        """Чтение датчиков и публикация данных"""
        # НЕ отправляем данные, пока не получена конфигурация
        if not self.config_received:
            return

        # НЕ отправляем, если частоты не установлены
        if self.sampling_temp == -1 and self.sampling_noise == -1:
            return

        current_time = time.time()
        temp = -1
        temp_time = int(current_time)
        noise = -1
        noise_time = int(current_time)

        # Проверка времени для температуры
        if self.sampling_temp != -1 and current_time - self.last_temp_reading >= self.sampling_temp:
            print(f"[SENSOR] Reading temperature (interval: {self.sampling_temp}s)...")
            temp = self.temp_sensor.read_temperature() if self.temp_sensor else -1
            temp_time = int(current_time)
            self.last_temp_reading = current_time
            print(f"[SENSOR] Temperature = {temp} C")

            # Проверка на критические ошибки датчика
            if temp == -1 and self.temp_sensor:
                self.errors.append("Temperature sensor failure")
                self.critical_error_detected = True
                print("[ERROR] Temperature sensor returned -1 (failure)")

        # Проверка времени для шума
        if self.sampling_noise != -1 and current_time - self.last_noise_reading >= self.sampling_noise:
            print(f"[SENSOR] Reading noise (interval: {self.sampling_noise}s)...")
            noise = self.noise_sensor.read_noise() if self.noise_sensor else -1
            noise_time = int(current_time)
            self.last_noise_reading = current_time
            print(f"[SENSOR] Noise = {noise} dB")

            # Проверка на критические ошибки датчика
            if noise == -1 and self.noise_sensor:
                self.errors.append("Noise sensor failure")
                self.critical_error_detected = True
                print("[ERROR] Noise sensor returned -1 (failure)")

        # Если есть новые данные - отправляем
        if temp != -1 or noise != -1:
            data = {
                "temperature": temp,
                "temperature_time": temp_time,
                "noise": noise,
                "noise_time": noise_time
            }

            topic = f"/device/{DEVICE_ID}/data"
            payload = ujson.dumps(data)

            try:
                print(f"[MQTT] Publishing data to topic: {topic}")
                # QoS 2 для данных (максимальная надежность)
                self.mqtt.publish(topic, payload, qos=2)
                print(f"[DATA] [OK] Published (QoS 2): {payload}")
            except Exception as e:
                print(f"[ERROR] [FAIL] Publish data failed: {e}")
                sys.print_exception(e)
                self.errors.append(f"Publish error: {e}")
                self.critical_error_detected = True

    def publish_status(self, force=False):
        """
        Публикация статуса устройства

        :param force: Принудительная отправка (игнорирует таймер)
        """
        # НЕ отправляем статус, пока не получена конфигурация
        if not self.config_received:
            return

        # НЕ отправляем, если частота не установлена (кроме force)
        if self.status_frequency == -1 and not force:
            return

        current_time = time.time()

        # Проверка времени (если не force)
        if not force and current_time - self.last_status_send < self.status_frequency:
            return

        self.last_status_send = current_time

        print("[STATUS] Collecting device status information...")

        # Сбор данных статуса
        battery = self.battery_sensor.read_percentage() if self.battery_sensor else -1
        signal = self.sim800l.get_signal_quality() if self.sim800l else -1

        print(f"[STATUS] Battery level = {battery}%")
        print(f"[STATUS] Signal strength = {signal}%")

        # Проверка критических значений
        if battery != -1 and battery < 15:
            self.errors.append(f"Critical battery level: {battery}%")
            print(f"[WARNING] Critical battery level: {battery}%")

        if signal != -1 and signal < 5:
            self.errors.append(f"Critical signal strength: {signal}%")
            print(f"[WARNING] Critical signal strength: {signal}%")

        status = {
            "battery_level": battery,
            "signal_strength": signal,
            "timestamp": int(current_time),
            "errors": self.errors.copy()
        }

        topic = f"/device/{DEVICE_ID}/status"
        payload = ujson.dumps(status)

        try:
            print(f"[MQTT] Publishing status to topic: {topic}")
            # QoS 1 для статуса (достаточно)
            self.mqtt.publish(topic, payload, qos=1)
            if force:
                print(f"[STATUS] [OK] Published (FORCED): {payload}")
            else:
                print(f"[STATUS] [OK] Published: {payload}")
            # Очищаем ошибки после отправки
            self.errors.clear()
            self.critical_error_detected = False
        except Exception as e:
            print(f"[ERROR] [FAIL] Publish status failed: {e}")
            sys.print_exception(e)

    def check_config_updates(self):
        """Проверка обновлений конфигурации"""
        try:
            msg = self.mqtt.check_messages()
            if msg:
                topic, payload = msg
                print(f"[MQTT] Message received on topic: {topic}")
                print(f"[MQTT] Payload: {payload}")

                # Проверяем, что это наш топик конфигурации
                if topic == f"/device/{DEVICE_ID}/config":
                    self.handle_config(payload)
                else:
                    print(f"[MQTT] Ignoring message from unknown topic")

        except Exception as e:
            print(f"[ERROR] Check messages failed: {e}")
            sys.print_exception(e)

    def handle_config(self, payload):
        """Обработка конфигурации от сервера"""
        try:
            print("[CONFIG] Parsing configuration payload...")
            config = ujson.loads(payload)
            print(f"[CONFIG] Received configuration: {config}")

            # ВАЖНО: Сначала проверяем delete_device!
            if "delete_device" in config and config["delete_device"]:
                print("[CONFIG] Delete device requested!")
                self.delete_requested = True
                self.reset_config()
                return  # Прерываем обработку остальных параметров

            # Помечаем, что конфигурация получена (активируем датчик)
            self.config_received = True
            print("[CONFIG] Configuration received flag set to True")

            # Обновление параметров
            if "sampling_rate_noise" in config:
                value = config["sampling_rate_noise"]
                if value != -1:
                    self.sampling_noise = value
                    print(f"[CONFIG] Noise sampling rate = {self.sampling_noise}s")
                else:
                    # Если пришло -1, отключаем опрос шума
                    self.sampling_noise = -1
                    print(f"[CONFIG] Noise sampling = DISABLED")

            if "sampling_rate_temperature" in config:
                value = config["sampling_rate_temperature"]
                if value != -1:
                    self.sampling_temp = value
                    print(f"[CONFIG] Temperature sampling rate = {self.sampling_temp}s")
                else:
                    # Если пришло -1, отключаем опрос температуры
                    self.sampling_temp = -1
                    print(f"[CONFIG] Temperature sampling = DISABLED")

            if "frequency_status" in config:
                value = config["frequency_status"]
                if value != -1:
                    self.status_frequency = value
                    print(f"[CONFIG] Status frequency = {self.status_frequency}s")
                else:
                    # Если пришло -1, отключаем отправку статуса
                    self.status_frequency = -1
                    print(f"[CONFIG] Status frequency = DISABLED")

            if "restart_device" in config and config["restart_device"]:
                print("[CONFIG] Restart device requested!")
                self.restart_requested = True

            if "health_check" in config and config["health_check"]:
                print("[CONFIG] Health check requested!")
                self.health_check_requested = True
                self.perform_health_check()

            # СОХРАНЯЕМ конфигурацию в энергонезависимую память
            print("[CONFIG] Saving configuration to flash memory...")
            self.save_config()

        except Exception as e:
            print(f"[ERROR] Config parsing failed: {e}")
            sys.print_exception(e)
            self.errors.append(f"Config parse error: {e}")

    def save_config(self):
        """Сохранение конфигурации в энергонезависимую память"""
        try:
            config_data = {
                "config_received": self.config_received,
                "sampling_noise": self.sampling_noise,
                "sampling_temp": self.sampling_temp,
                "status_frequency": self.status_frequency
            }

            print(f"[CONFIG] Writing configuration to {CONFIG_FILE}...")
            with open(CONFIG_FILE, 'w') as f:
                ujson.dump(config_data, f)

            print(f"[CONFIG] [OK] Configuration saved to {CONFIG_FILE}")
        except Exception as e:
            print(f"[ERROR] [FAIL] Failed to save config: {e}")
            sys.print_exception(e)

    def load_config(self):
        """Загрузка конфигурации из энергонезависимой памяти"""
        try:
            print(f"[CONFIG] Checking for saved configuration file: {CONFIG_FILE}")
            if CONFIG_FILE in os.listdir():
                print(f"[CONFIG] Found {CONFIG_FILE}, loading...")
                with open(CONFIG_FILE, 'r') as f:
                    config_data = ujson.load(f)

                self.config_received = config_data.get("config_received", False)
                self.sampling_noise = config_data.get("sampling_noise", -1)
                self.sampling_temp = config_data.get("sampling_temp", -1)
                self.status_frequency = config_data.get("status_frequency", -1)

                print("[CONFIG] [OK] Configuration loaded from flash:")
                print(f"  - config_received: {self.config_received}")
                print(f"  - sampling_noise: {self.sampling_noise}")
                print(f"  - sampling_temp: {self.sampling_temp}")
                print(f"  - status_frequency: {self.status_frequency}")
            else:
                print("[CONFIG] No saved configuration found (first boot)")
        except Exception as e:
            print(f"[ERROR] Failed to load config: {e}")
            sys.print_exception(e)
            print("[CONFIG] Using default values")

    def reset_config(self):
        """Сброс конфигурации датчика (при delete_device)"""
        print("[CONFIG] Resetting device configuration...")

        # Сбрасываем все настройки
        self.config_received = False
        self.sampling_noise = -1
        self.sampling_temp = -1
        self.status_frequency = -1
        self.restart_requested = False
        self.health_check_requested = False
        self.delete_requested = False

        # Сбрасываем таймеры
        self.last_temp_reading = 0
        self.last_noise_reading = 0
        self.last_status_send = 0

        # Очищаем ошибки
        self.errors.clear()

        # УДАЛЯЕМ сохраненную конфигурацию
        try:
            if CONFIG_FILE in os.listdir():
                os.remove(CONFIG_FILE)
                print(f"[CONFIG] [OK] Saved configuration deleted from flash")
        except Exception as e:
            print(f"[ERROR] Failed to delete config file: {e}")
            sys.print_exception(e)

        print("[CONFIG] Device reset complete. Waiting for new config...")

    def perform_health_check(self):
        """Выполнение комплексной проверки здоровья устройства"""
        print("[HEALTH] ===== Performing comprehensive health check =====")

        health_errors = []

        # 1. Проверка температурного датчика
        print("[HEALTH] Checking temperature sensor...")
        if self.temp_sensor:
            temp = self.temp_sensor.read_temperature()
            if temp == -1:
                health_errors.append("Temperature sensor: FAIL")
                print("[HEALTH] [FAIL] Temperature sensor: FAIL")
            else:
                print(f"[HEALTH] [OK] Temperature sensor: OK ({temp} C)")
        else:
            health_errors.append("Temperature sensor: NOT INITIALIZED")
            print("[HEALTH] [FAIL] Temperature sensor: NOT INITIALIZED")

        # 2. Проверка датчика шума
        print("[HEALTH] Checking noise sensor...")
        if self.noise_sensor:
            noise = self.noise_sensor.read_noise()
            if noise == -1:
                health_errors.append("Noise sensor: FAIL")
                print("[HEALTH] [FAIL] Noise sensor: FAIL")
            else:
                print(f"[HEALTH] [OK] Noise sensor: OK ({noise} dB)")
        else:
            health_errors.append("Noise sensor: NOT INITIALIZED")
            print("[HEALTH] [FAIL] Noise sensor: NOT INITIALIZED")

        # 3. Проверка уровня заряда батареи
        print("[HEALTH] Checking battery level...")
        if self.battery_sensor:
            battery = self.battery_sensor.read_percentage()
            if battery == -1:
                health_errors.append("Battery sensor: FAIL")
                print("[HEALTH] [FAIL] Battery sensor: FAIL")
            else:
                print(f"[HEALTH] [OK] Battery level: {battery}%")
                if battery < 20:
                    health_errors.append(f"Battery LOW: {battery}%")
                    print(f"[HEALTH] [WARN] Battery LOW: {battery}%")
                if battery < 10:
                    health_errors.append(f"Battery CRITICAL: {battery}%")
                    print(f"[HEALTH] [WARN] Battery CRITICAL: {battery}%")
        else:
            health_errors.append("Battery sensor: NOT INITIALIZED")
            print("[HEALTH] [FAIL] Battery sensor: NOT INITIALIZED")

        # 4. Проверка уровня GSM сигнала
        print("[HEALTH] Checking GSM signal strength...")
        if self.sim800l:
            signal = self.sim800l.get_signal_quality()
            if signal == -1:
                health_errors.append("GSM signal: NO SIGNAL")
                print("[HEALTH] [FAIL] GSM signal: NO SIGNAL")
            else:
                print(f"[HEALTH] [OK] GSM signal: {signal}%")
                if signal < 10:
                    health_errors.append(f"GSM signal WEAK: {signal}%")
                    print(f"[HEALTH] [WARN] GSM signal WEAK: {signal}%")
        else:
            health_errors.append("SIM800L: NOT INITIALIZED")
            print("[HEALTH] [FAIL] SIM800L: NOT INITIALIZED")

        # 5. Проверка MQTT соединения
        print("[HEALTH] Checking MQTT connection...")
        if self.mqtt:
            if self.mqtt.connected:
                print("[HEALTH] [OK] MQTT connection: OK")
            else:
                health_errors.append("MQTT: DISCONNECTED")
                print("[HEALTH] [FAIL] MQTT connection: DISCONNECTED")
        else:
            health_errors.append("MQTT: NOT INITIALIZED")
            print("[HEALTH] [FAIL] MQTT: NOT INITIALIZED")

        # 6. Проверка доступной памяти
        print("[HEALTH] Checking available memory...")
        try:
            import gc
            gc.collect()
            free_mem = gc.mem_free()
            print(f"[HEALTH] [OK] Free memory: {free_mem} bytes")
            if free_mem < 10000:  # Менее 10KB
                health_errors.append(f"Low memory: {free_mem} bytes")
                print(f"[HEALTH] [WARN] Low memory: {free_mem} bytes")
        except Exception as e:
            health_errors.append(f"Memory check failed: {e}")
            print(f"[HEALTH] [FAIL] Memory check failed: {e}")

        # 7. Проверка конфигурационного файла
        print("[HEALTH] Checking configuration file...")
        try:
            if CONFIG_FILE in os.listdir():
                print("[HEALTH] [OK] Config file: OK")
            else:
                health_errors.append("Config file: NOT FOUND")
                print("[HEALTH] [FAIL] Config file: NOT FOUND")
        except Exception as e:
            health_errors.append(f"Config file check failed: {e}")
            print(f"[HEALTH] [FAIL] Config file check failed: {e}")

        # Добавление ошибок в общий список
        if health_errors:
            self.errors.extend(health_errors)
            print(f"[HEALTH] Found {len(health_errors)} issue(s)")
            # Устанавливаем флаг для немедленной отправки статуса
            self.critical_error_detected = True
        else:
            print("[HEALTH] [OK] All systems operational")

        print("[HEALTH] Health check completed\n")
        self.health_check_requested = False

    def run(self):
        """Основной цикл работы"""
        print("\n[MAIN] ===== Starting main loop =====\n")

        # Инициализация датчиков
        print("[MAIN] Step 1 of 2: Initializing sensors...")
        try:
            self.init_sensors()
            print("[MAIN] [OK] Sensors initialization completed\n")
        except Exception as e:
            print(f"[FATAL ERROR] [FAIL] Sensor initialization failed: {e}")
            sys.print_exception(e)
            print("[FATAL] Cannot continue without sensors")
            return

        # Инициализация канала связи (GSM или WiFi с приоритетом)
        print("[MAIN] Step 2 of 2: Initializing communication channel...")
        try:
            if not self.init_communication():
                print("[FATAL] [FAIL] Communication initialization failed!")
                print("[FATAL] Cannot continue without communication")
                print("[FATAL] Check GSM/WiFi settings and network availability")
                return
            print(f"[MAIN] [OK] Communication channel ready ({self.communication_method.upper()})\n")
        except Exception as e:
            print(f"[FATAL ERROR] [FAIL] Communication initialization exception: {e}")
            sys.print_exception(e)
            return

        # Проверка наличия сохраненной конфигурации
        if self.config_received:
            print("[MAIN] [OK] Using saved configuration from previous session")
            print("[MAIN] Device will resume work with saved settings")
        else:
            print("[MAIN] [!] No saved configuration found")
            print("[MAIN] [!] Waiting for configuration from server...")
            print("[MAIN] [!] Device will start sending data after receiving config")

        print("\n[MAIN] ===== Entering main loop =====\n")
        loop_iteration = 0

        # Главный цикл
        while True:
            try:
                loop_iteration += 1
                print(f"\n[LOOP] ----- Iteration #{loop_iteration} -----")
                print(f"[LOOP] Using communication: {self.communication_method.upper()}")

                # Чтение и публикация данных
                print("[LOOP] Step 1/4: Reading and publishing sensor data...")
                self.read_and_publish_data()

                # Проверка критических ошибок - немедленная отправка статуса
                if self.critical_error_detected and self.config_received:
                    print("[MAIN] [WARN] Critical error detected! Sending status immediately...")
                    self.publish_status(force=True)

                # Публикация статуса по расписанию
                print("[LOOP] Step 2/4: Publishing device status...")
                self.publish_status()

                # Проверка конфигурации
                print("[LOOP] Step 3/4: Checking for config updates...")
                self.check_config_updates()

                # Проверка запроса на перезагрузку
                if self.restart_requested:
                    print("[MAIN] [WARN] Restart requested! Rebooting in 2 seconds...")
                    time.sleep(2)
                    machine.reset()

                # Энергосбережение: интеллектуальный сон
                print("[LOOP] Step 4/4: Power management...")
                if self.config_received:
                    # Рассчитываем оптимальное время сна
                    deep_sleep_ms = calculate_deep_sleep_time(
                        self.sampling_temp,
                        self.sampling_noise
                    )

                    if deep_sleep_ms > 60000:  # Если сон > 1 минуты - используем deepsleep
                        print(f"[POWER] Entering deep sleep for {deep_sleep_ms // 1000}s")
                        # Отключаем периферию перед сном
                        if self.mqtt and self.mqtt.connected:
                            # Не отключаем MQTT - будет reconnect после пробуждения
                            pass
                        # Deep sleep - потребление <100 мкА
                        machine.deepsleep(deep_sleep_ms)
                    else:
                        # Короткие паузы - обычный sleep
                        sleep_time = max(0.1, deep_sleep_ms / 1000)
                        print(f"[POWER] Light sleep for {sleep_time:.1f}s")
                        time.sleep(sleep_time)
                else:
                    # Если ждем конфигурацию - пауза 1 сек
                    print("[POWER] Waiting for config, sleeping 1s...")
                    time.sleep(1)

            except KeyboardInterrupt:
                print("\n[MAIN] [WARN] Interrupted by user (Ctrl+C)")
                break

            except Exception as e:
                print(f"\n[ERROR] [FAIL] Main loop exception: {e}")
                sys.print_exception(e)
                self.errors.append(f"Main loop: {e}")
                print("[ERROR] Sleeping 5s before retry...")
                time.sleep(5)

        # Завершение
        print("\n[MAIN] ===== Shutting down device =====")

        if self.mqtt:
            print(f"[MAIN] Disconnecting MQTT ({self.communication_method.upper()})...")
            try:
                self.mqtt.disconnect()
                print("[MAIN] [OK] MQTT disconnected")
            except Exception as e:
                print(f"[ERROR] MQTT disconnect failed: {e}")

        if self.communication_method == "gsm" and self.sim800l:
            print("[MAIN] Disconnecting GPRS...")
            try:
                self.sim800l.disconnect_gprs()
                print("[MAIN] [OK] GPRS disconnected")
            except Exception as e:
                print(f"[ERROR] GPRS disconnect failed: {e}")

        if self.communication_method == "wifi" and self.wifi:
            print("[MAIN] Disconnecting WiFi...")
            try:
                self.wifi.disconnect()
                print("[MAIN] [OK] WiFi disconnected")
            except Exception as e:
                print(f"[ERROR] WiFi disconnect failed: {e}")

        print("[MAIN] [OK] Device stopped gracefully\n")


# Точка входа
if __name__ == "__main__":
    print("\n[BOOT] ===== Starting BeeIoT application =====")
    print("[BOOT] Creating BeeIoTDevice instance...")
    try:
        device = BeeIoTDevice()
        print("[BOOT] [OK] Device instance created successfully")
        print("[BOOT] Starting device.run()...\n")
        device.run()
    except Exception as e:
        print(f"\n[FATAL ERROR] [FAIL] Application crashed: {e}")
        sys.print_exception(e)
        print("[FATAL] Device will not restart automatically")
        print("[FATAL] Please check the error above and fix the issue\n")

    print("[BOOT] ===== Application terminated =====\n")
