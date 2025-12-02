# -*- coding: utf-8 -*-
"""
Драйверы для датчиков:
- DS18B20 (температура)
- KY-038 (шум)
"""

from machine import Pin, ADC
import onewire
import ds18x20
import time
import math


class DS18B20Sensor:
    """Драйвер для DS18B20 температурного датчика"""
    
    def __init__(self, pin):
        """
        Инициализация DS18B20
        
        :param pin: GPIO пин для OneWire шины
        """
        self.ow = onewire.OneWire(Pin(pin))
        self.ds = ds18x20.DS18X20(self.ow)
        self.devices = []
        self.scan_devices()
    
    def scan_devices(self):
        """Сканирование устройств на шине"""
        print("[DS18B20] Scanning for devices...")
        self.devices = self.ds.scan()
        print(f"[DS18B20] Found {len(self.devices)} device(s)")
        return len(self.devices)
    
    def read_temperature(self):
        """
        Чтение температуры
        
        :return: Температура в °C или -1 при ошибке
        """
        if not self.devices:
            self.scan_devices()
            if not self.devices:
                print("[DS18B20] ERROR: No devices found")
                return -1
        
        try:
            # Запуск конвертации
            self.ds.convert_temp()
            time.sleep_ms(750)  # Ожидание завершения конвертации
            
            # Чтение с первого датчика
            temp = self.ds.read_temp(self.devices[0])
            print(f"[DS18B20] Temperature: {temp:.2f}°C")
            return round(temp, 2)
        
        except Exception as e:
            print(f"[DS18B20] ERROR: {e}")
            return -1


class KY038Sensor:
    """Драйвер для KY-038 датчика шума"""
    
    def __init__(self, analog_pin, digital_pin=None, min_db=40, max_db=120):
        """
        Инициализация KY-038
        
        :param analog_pin: GPIO пин для аналогового выхода (ADC)
        :param digital_pin: GPIO пин для цифрового выхода (опционально)
        :param min_db: Минимальный уровень шума в дБ
        :param max_db: Максимальный уровень шума в дБ
        """
        # Аналоговый вход
        self.adc = ADC(Pin(analog_pin))
        self.adc.atten(ADC.ATTN_11DB)  # Диапазон 0-3.3V
        self.adc.width(ADC.WIDTH_12BIT)  # 12-bit разрешение (0-4095)
        
        # Цифровой вход (опционально)
        self.digital_pin = Pin(digital_pin, Pin.IN) if digital_pin else None
        
        # Калибровка
        self.min_db = min_db
        self.max_db = max_db
        self.adc_max = 4095
    
    def read_raw(self):
        """
        Чтение сырого значения ADC
        
        :return: Значение ADC 0-4095
        """
        return self.adc.read()
    
    def read_noise(self, samples=10):
        """
        Чтение уровня шума в дБ
        Усредняет несколько измерений для стабильности
        
        :param samples: Количество измерений для усреднения
        :return: Уровень шума в дБ или -1 при ошибке
        """
        try:
            values = []
            for _ in range(samples):
                values.append(self.adc.read())
                time.sleep_ms(10)
            
            # Среднее значение
            avg_value = sum(values) / len(values)
            
            # Преобразование ADC в дБ (линейная аппроксимация)
            # ВАЖНО: Требуется калибровка для вашего конкретного датчика!
            db = self.min_db + (avg_value / self.adc_max) * (self.max_db - self.min_db)
            
            db = round(db, 1)
            print(f"[KY-038] Noise: {db} dB (ADC: {avg_value:.0f})")
            return db
        
        except Exception as e:
            print(f"[KY-038] ERROR: {e}")
            return -1
    
    def is_noise_detected(self):
        """
        Проверка цифрового выхода (порог)
        
        :return: True если шум превышает порог
        """
        if self.digital_pin:
            return self.digital_pin.value() == 0  # Обычно LOW = шум обнаружен
        return False


class BatterySensor:
    """Датчик уровня заряда батареи (через делитель напряжения)"""
    
    def __init__(self, adc_pin=None, min_voltage=3.0, max_voltage=4.2):
        """
        Инициализация датчика батареи
        
        :param adc_pin: GPIO пин для измерения напряжения (или None если не используется)
        :param min_voltage: Минимальное напряжение (0%)
        :param max_voltage: Максимальное напряжение (100%)
        """
        self.adc = None
        if adc_pin is not None:
            self.adc = ADC(Pin(adc_pin))
            self.adc.atten(ADC.ATTN_11DB)
            self.adc.width(ADC.WIDTH_12BIT)
        
        self.min_voltage = min_voltage
        self.max_voltage = max_voltage
        self.adc_max = 4095
        self.vref = 3.3  # Опорное напряжение ADC
    
    def read_voltage(self):
        """
        Чтение напряжения батареи
        
        :return: Напряжение в вольтах или -1
        """
        if not self.adc:
            return -1
        
        try:
            raw = self.adc.read()
            # Преобразование ADC в вольты
            # ВАЖНО: Учтите коэффициент делителя напряжения!
            voltage = (raw / self.adc_max) * self.vref
            return round(voltage, 2)
        except:
            return -1
    
    def read_percentage(self):
        """
        Чтение уровня заряда в процентах
        
        :return: Уровень заряда 0-100% или -1
        """
        if not self.adc:
            # Если нет датчика, возвращаем фиктивное значение
            # В реальности можно подключить к внутреннему ADC или использовать внешний чип
            return -1
        
        voltage = self.read_voltage()
        if voltage < 0:
            return -1
        
        # Преобразование напряжения в проценты
        percentage = ((voltage - self.min_voltage) / (self.max_voltage - self.min_voltage)) * 100
        percentage = max(0, min(100, percentage))  # Ограничение 0-100
        
        return int(percentage)
