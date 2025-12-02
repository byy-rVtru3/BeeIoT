# -*- coding: utf-8 -*-
"""
BeeIoT - Прошивка для датчика улья на ESP32-S3
Отправка данных о температуре и шуме через MQTT по GPRS
"""

import time
import ujson
import machine
import os
from config import *
from sim800l import SIM800L
from sensors import DS18B20Sensor, KY038Sensor, BatterySensor
from mqtt_client import SimpleMQTT

# Файл для сохранения конфигурации
CONFIG_FILE = "device_config.json"


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
        print("\n" + "="*50)
        print("BeeIoT Device Starting...")
        print("="*50 + "\n")
        
        # Датчики
        self.temp_sensor = None
        self.noise_sensor = None
        self.battery_sensor = None
        
        # Связь
        self.sim800l = None
        self.mqtt = None
        
        # Конфигурация (изначально -1 = не установлено)
        self.sampling_noise = -1
        self.sampling_temp = -1
        self.status_frequency = -1
        self.restart_requested = False
        self.health_check_requested = False
        self.delete_requested = False
        
        # Флаг инициализации от сервера
        self.config_received = False
        
        # Таймеры
        self.last_temp_reading = 0
        self.last_noise_reading = 0
        self.last_status_send = 0
        
        # Ошибки
        self.errors = []
        
        # Флаг для немедленной отправки статуса при критических ошибках
        self.critical_error_detected = False
        
        # Загрузка сохраненной конфигурации (если есть)
        self.load_config()
    
    def init_sensors(self):
        """Инициализация всех датчиков"""
        print("[INIT] Initializing sensors...")
        
        try:
            # DS18B20 температура
            self.temp_sensor = DS18B20Sensor(DS18B20_PIN)
            print("[INIT] DS18B20 initialized")
        except Exception as e:
            self.errors.append(f"DS18B20 init error: {e}")
            print(f"[ERROR] DS18B20: {e}")
        
        try:
            # KY-038 шум
            self.noise_sensor = KY038Sensor(
                KY038_ANALOG_PIN,
                KY038_DIGITAL_PIN,
                NOISE_MIN_DB,
                NOISE_MAX_DB
            )
            print("[INIT] KY-038 initialized")
        except Exception as e:
            self.errors.append(f"KY-038 init error: {e}")
            print(f"[ERROR] KY-038: {e}")
        
        try:
            # Батарея
            self.battery_sensor = BatterySensor(
                BATTERY_ADC_PIN,
                BATTERY_MIN_VOLTAGE,
                BATTERY_MAX_VOLTAGE
            )
            print("[INIT] Battery sensor initialized")
        except Exception as e:
            print(f"[WARNING] Battery sensor: {e}")
    
    def init_sim800l(self):
        """Инициализация SIM800L и подключение к GPRS"""
        print("[INIT] Initializing SIM800L...")
        
        try:
            self.sim800l = SIM800L(
                SIM800L_UART_ID,
                SIM800L_TX_PIN,
                SIM800L_RX_PIN,
                SIM800L_BAUDRATE,
                SIM800L_RST_PIN
            )
            self.sim800l.debug = DEBUG
            
            # Инициализация модуля
            if not self.sim800l.init():
                self.errors.append("SIM800L init failed")
                return False
            
            # Подключение к GPRS
            if not self.sim800l.connect_gprs(APN, APN_USER, APN_PASSWORD):
                self.errors.append("GPRS connection failed")
                return False
            
            print("[INIT] SIM800L ready")
            return True
            
        except Exception as e:
            self.errors.append(f"SIM800L error: {e}")
            print(f"[ERROR] SIM800L: {e}")
            return False
    
    def init_mqtt(self):
        """Инициализация MQTT клиента"""
        print("[INIT] Initializing MQTT...")
        
        try:
            self.mqtt = SimpleMQTT(
                self.sim800l,
                DEVICE_ID,
                MQTT_BROKER,
                MQTT_PORT,
                MQTT_USER,
                MQTT_PASSWORD,
                MQTT_KEEPALIVE
            )
            
            # Подключение к MQTT брокеру
            if not self.mqtt.connect():
                self.errors.append("MQTT connection failed")
                return False
            
            # Подписка на топик конфигурации
            config_topic = f"/device/{DEVICE_ID}/config"
            if not self.mqtt.subscribe(config_topic, qos=1):
                print(f"[WARNING] Failed to subscribe to {config_topic}")
            
            print("[INIT] MQTT ready")
            return True
            
        except Exception as e:
            self.errors.append(f"MQTT error: {e}")
            print(f"[ERROR] MQTT: {e}")
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
            temp = self.temp_sensor.read_temperature() if self.temp_sensor else -1
            temp_time = int(current_time)
            self.last_temp_reading = current_time
            
            # Проверка на критические ошибки датчика
            if temp == -1 and self.temp_sensor:
                self.errors.append("Temperature sensor failure")
                self.critical_error_detected = True
        
        # Проверка времени для шума
        if self.sampling_noise != -1 and current_time - self.last_noise_reading >= self.sampling_noise:
            noise = self.noise_sensor.read_noise() if self.noise_sensor else -1
            noise_time = int(current_time)
            self.last_noise_reading = current_time
            
            # Проверка на критические ошибки датчика
            if noise == -1 and self.noise_sensor:
                self.errors.append("Noise sensor failure")
                self.critical_error_detected = True
        
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
                # QoS 2 для данных (максимальная надежность)
                self.mqtt.publish(topic, payload, qos=2)
                print(f"[DATA] Published (QoS 2): {payload}")
            except Exception as e:
                print(f"[ERROR] Publish data failed: {e}")
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
        
        # Сбор данных статуса
        battery = self.battery_sensor.read_percentage() if self.battery_sensor else -1
        signal = self.sim800l.get_signal_quality() if self.sim800l else -1
        
        # Проверка критических значений
        if battery != -1 and battery < 15:
            self.errors.append(f"Critical battery level: {battery}%")
        
        if signal != -1 and signal < 5:
            self.errors.append(f"Critical signal strength: {signal}%")
        
        status = {
            "battery_level": battery,
            "signal_strength": signal,
            "timestamp": int(current_time),
            "errors": self.errors.copy()
        }
        
        topic = f"/device/{DEVICE_ID}/status"
        payload = ujson.dumps(status)
        
        try:
            # QoS 1 для статуса (достаточно)
            self.mqtt.publish(topic, payload, qos=1)
            if force:
                print(f"[STATUS] Published (FORCED): {payload}")
            else:
                print(f"[STATUS] Published: {payload}")
            # Очищаем ошибки после отправки
            self.errors.clear()
            self.critical_error_detected = False
        except Exception as e:
            print(f"[ERROR] Publish status failed: {e}")
    
    def check_config_updates(self):
        """Проверка обновлений конфигурации"""
        try:
            msg = self.mqtt.check_messages()
            if msg:
                topic, payload = msg
                
                # Проверяем, что это наш топик конфигурации
                if topic == f"/device/{DEVICE_ID}/config":
                    self.handle_config(payload)
        
        except Exception as e:
            print(f"[ERROR] Check messages failed: {e}")
    
    def handle_config(self, payload):
        """Обработка конфигурации от сервера"""
        try:
            config = ujson.loads(payload)
            print(f"[CONFIG] Received: {config}")
            
            # ВАЖНО: Сначала проверяем delete_device!
            if "delete_device" in config and config["delete_device"]:
                print("[CONFIG] Delete device requested!")
                self.delete_requested = True
                self.reset_config()
                return  # Прерываем обработку остальных параметров
            
            # Помечаем, что конфигурация получена (активируем датчик)
            self.config_received = True
            
            # Обновление параметров
            if "sampling_rate_noise" in config:
                value = config["sampling_rate_noise"]
                if value != -1:
                    self.sampling_noise = value
                    print(f"[CONFIG] Noise sampling: {self.sampling_noise}s")
                else:
                    # Если пришло -1, отключаем опрос шума
                    self.sampling_noise = -1
                    print(f"[CONFIG] Noise sampling: DISABLED")
            
            if "sampling_rate_temperature" in config:
                value = config["sampling_rate_temperature"]
                if value != -1:
                    self.sampling_temp = value
                    print(f"[CONFIG] Temperature sampling: {self.sampling_temp}s")
                else:
                    # Если пришло -1, отключаем опрос температуры
                    self.sampling_temp = -1
                    print(f"[CONFIG] Temperature sampling: DISABLED")
            
            if "frequency_status" in config:
                value = config["frequency_status"]
                if value != -1:
                    self.status_frequency = value
                    print(f"[CONFIG] Status frequency: {self.status_frequency}s")
                else:
                    # Если пришло -1, отключаем отправку статуса
                    self.status_frequency = -1
                    print(f"[CONFIG] Status frequency: DISABLED")
            
            if "restart_device" in config and config["restart_device"]:
                print("[CONFIG] Restart requested!")
                self.restart_requested = True
            
            if "health_check" in config and config["health_check"]:
                print("[CONFIG] Health check requested!")
                self.health_check_requested = True
                self.perform_health_check()
            
            # СОХРАНЯЕМ конфигурацию в энергонезависимую память
            self.save_config()
        
        except Exception as e:
            print(f"[ERROR] Config parsing failed: {e}")
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
            
            with open(CONFIG_FILE, 'w') as f:
                ujson.dump(config_data, f)
            
            print(f"[CONFIG] Configuration saved to {CONFIG_FILE}")
        except Exception as e:
            print(f"[ERROR] Failed to save config: {e}")
    
    def load_config(self):
        """Загрузка конфигурации из энергонезависимой памяти"""
        try:
            if CONFIG_FILE in os.listdir():
                with open(CONFIG_FILE, 'r') as f:
                    config_data = ujson.load(f)
                
                self.config_received = config_data.get("config_received", False)
                self.sampling_noise = config_data.get("sampling_noise", -1)
                self.sampling_temp = config_data.get("sampling_temp", -1)
                self.status_frequency = config_data.get("status_frequency", -1)
                
                print("[CONFIG] Configuration loaded from flash:")
                print(f"  - config_received: {self.config_received}")
                print(f"  - sampling_noise: {self.sampling_noise}")
                print(f"  - sampling_temp: {self.sampling_temp}")
                print(f"  - status_frequency: {self.status_frequency}")
            else:
                print("[CONFIG] No saved configuration found (first boot)")
        except Exception as e:
            print(f"[ERROR] Failed to load config: {e}")
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
                print(f"[CONFIG] Saved configuration deleted from flash")
        except Exception as e:
            print(f"[ERROR] Failed to delete config file: {e}")
        
        print("[CONFIG] Device reset complete. Waiting for new config...")
    
    def perform_health_check(self):
        """Выполнение комплексной проверки здоровья устройства"""
        print("[HEALTH] Performing comprehensive health check...")
        
        health_errors = []
        
        # 1. Проверка температурного датчика
        if self.temp_sensor:
            temp = self.temp_sensor.read_temperature()
            if temp == -1:
                health_errors.append("Temperature sensor: FAIL")
                print("[HEALTH] Temperature sensor: FAIL")
            else:
                print(f"[HEALTH] Temperature sensor: OK ({temp}°C)")
        else:
            health_errors.append("Temperature sensor: NOT INITIALIZED")
        
        # 2. Проверка датчика шума
        if self.noise_sensor:
            noise = self.noise_sensor.read_noise()
            if noise == -1:
                health_errors.append("Noise sensor: FAIL")
                print("[HEALTH] Noise sensor: FAIL")
            else:
                print(f"[HEALTH] Noise sensor: OK ({noise} dB)")
        else:
            health_errors.append("Noise sensor: NOT INITIALIZED")
        
        # 3. Проверка уровня заряда батареи
        if self.battery_sensor:
            battery = self.battery_sensor.read_percentage()
            if battery == -1:
                health_errors.append("Battery sensor: FAIL")
                print("[HEALTH] Battery sensor: FAIL")
            else:
                print(f"[HEALTH] Battery level: {battery}%")
                if battery < 20:
                    health_errors.append(f"Battery LOW: {battery}%")
                if battery < 10:
                    health_errors.append(f"Battery CRITICAL: {battery}%")
        else:
            health_errors.append("Battery sensor: NOT INITIALIZED")
        
        # 4. Проверка уровня GSM сигнала
        if self.sim800l:
            signal = self.sim800l.get_signal_quality()
            if signal == -1:
                health_errors.append("GSM signal: NO SIGNAL")
                print("[HEALTH] GSM signal: NO SIGNAL")
            else:
                print(f"[HEALTH] GSM signal: {signal}%")
                if signal < 10:
                    health_errors.append(f"GSM signal WEAK: {signal}%")
        else:
            health_errors.append("SIM800L: NOT INITIALIZED")
        
        # 5. Проверка MQTT соединения
        if self.mqtt:
            if self.mqtt.connected:
                print("[HEALTH] MQTT connection: OK")
            else:
                health_errors.append("MQTT: DISCONNECTED")
                print("[HEALTH] MQTT connection: FAIL")
        else:
            health_errors.append("MQTT: NOT INITIALIZED")
        
        # 6. Проверка доступной памяти
        try:
            import gc
            gc.collect()
            free_mem = gc.mem_free()
            print(f"[HEALTH] Free memory: {free_mem} bytes")
            if free_mem < 10000:  # Менее 10KB
                health_errors.append(f"Low memory: {free_mem} bytes")
        except Exception as e:
            health_errors.append(f"Memory check failed: {e}")
        
        # 7. Проверка конфигурационного файла
        try:
            if CONFIG_FILE in os.listdir():
                print("[HEALTH] Config file: OK")
            else:
                health_errors.append("Config file: NOT FOUND")
        except Exception as e:
            health_errors.append(f"Config file check failed: {e}")
        
        # Добавление ошибок в общий список
        if health_errors:
            self.errors.extend(health_errors)
            print(f"[HEALTH] Found {len(health_errors)} issue(s)")
            # Устанавливаем флаг для немедленной отправки статуса
            self.critical_error_detected = True
        else:
            print("[HEALTH] All systems operational")
        
        print("[HEALTH] Health check completed")
        self.health_check_requested = False
    
    def run(self):
        """Основной цикл работы"""
        print("\n[MAIN] Starting main loop...\n")
        
        # Инициализация
        self.init_sensors()
        
        if not self.init_sim800l():
            print("[FATAL] SIM800L initialization failed!")
            return
        
        if not self.init_mqtt():
            print("[FATAL] MQTT initialization failed!")
            return
        
        # Проверка наличия сохраненной конфигурации
        if self.config_received:
            print("[MAIN] Using saved configuration from previous session")
            print("[MAIN] Device will resume work with saved settings")
        else:
            print("[MAIN] No saved configuration found")
            print("[MAIN] Waiting for configuration from server...")
            print("[MAIN] Device will start sending data after receiving config")
        
        # Главный цикл
        while True:
            try:
                # Чтение и публикация данных
                self.read_and_publish_data()
                
                # Проверка критических ошибок - немедленная отправка статуса
                if self.critical_error_detected and self.config_received:
                    print("[MAIN] Critical error detected! Sending status immediately...")
                    self.publish_status(force=True)
                
                # Публикация статуса по расписанию
                self.publish_status()
                
                # Проверка конфигурации
                self.check_config_updates()
                
                # Проверка запроса на перезагрузку
                if self.restart_requested:
                    print("[MAIN] Restarting device...")
                    time.sleep(2)
                    machine.reset()
                
                # Проверка delete_device - датчик переходит в режим ожидания
                # (delete_requested обрабатывается в handle_config через reset_config)
                
                # Энергосбережение: интеллектуальный сон
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
                        # Минимум 100мс для проверки входящих сообщений
                        time.sleep(max(0.1, deep_sleep_ms / 1000))
                else:
                    # Если ждем конфигурацию - пауза 1 сек
                    time.sleep(1)
            
            except KeyboardInterrupt:
                print("\n[MAIN] Interrupted by user")
                break
            
            except Exception as e:
                print(f"[ERROR] Main loop: {e}")
                self.errors.append(f"Main loop: {e}")
                time.sleep(5)
        
        # Завершение
        if self.mqtt:
            self.mqtt.disconnect()
        
        if self.sim800l:
            self.sim800l.disconnect_gprs()
        
        print("[MAIN] Device stopped")


# Точка входа
if __name__ == "__main__":
    device = BeeIoTDevice()
    device.run()
