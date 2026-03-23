# -*- coding: utf-8 -*-
"""
Sensor drivers:
- DS18B20 (temperature)
- KY-038 (noise)
"""

from machine import Pin, ADC
import onewire
import ds18x20
import time
import math


class DS18B20Sensor:
    """Driver for DS18B20 temperature sensor"""

    def __init__(self, pin):
        """
        Initialize DS18B20

        :param pin: GPIO pin for OneWire bus
        """
        self.ow = onewire.OneWire(Pin(pin))
        self.ds = ds18x20.DS18X20(self.ow)
        self.devices = []
        self.pin = pin
        self.scan_devices()

    def scan_devices(self):
        """Scan devices on the bus"""
        print("[DS18B20] Scanning for devices...")
        self.devices = self.ds.scan()
        print(f"[DS18B20] Found {len(self.devices)} device(s)")
        if self.devices:
            for i, device in enumerate(self.devices):
                # Output device address in hex format
                device_addr = ''.join(['%02X' % b for b in device])
                print(f"[DS18B20] Device {i}: {device_addr}")
        return len(self.devices)

    def read_temperature(self, device_index=0, verbose=True):
        """
        Read temperature

        :param device_index: Device index (if multiple sensors on bus)
        :param verbose: Print detailed information
        :return: Temperature in C or -1 on error
        """
        if not self.devices:
            self.scan_devices()
            if not self.devices:
                print("[DS18B20] ERROR: No devices found")
                return -1

        if device_index >= len(self.devices):
            print(f"[DS18B20] ERROR: Device index {device_index} out of range (max: {len(self.devices)-1})")
            return -1

        try:
            # Start conversion
            if verbose:
                print(f"[DS18B20] Starting temperature conversion (pin GPIO{self.pin})...")
            self.ds.convert_temp()
            time.sleep_ms(750)  # Wait for conversion to complete

            # Read temperature
            temp = self.ds.read_temp(self.devices[device_index])
            temp_rounded = round(temp, 2)

            if verbose:
                print(f"[DS18B20] OK Temperature reading successful")
                print(f"[DS18B20]   Raw value: {temp}C")
                print(f"[DS18B20]   Rounded: {temp_rounded}C")

                # Temperature analysis
                if temp_rounded < 0:
                    print(f"[DS18B20]   Status: FREEZING")
                elif temp_rounded < 10:
                    print(f"[DS18B20]   Status: Very Cold")
                elif temp_rounded < 20:
                    print(f"[DS18B20]   Status: Cool")
                elif temp_rounded < 30:
                    print(f"[DS18B20]   Status: Comfortable")
                elif temp_rounded < 40:
                    print(f"[DS18B20]   Status: Warm")
                else:
                    print(f"[DS18B20]   Status: HOT")
            else:
                print(f"[DS18B20] Temperature: {temp_rounded}C")

            return temp_rounded

        except Exception as e:
            print(f"[DS18B20] ERROR: {e}")
            return -1

    def read_all_temperatures(self):
        """
        Read temperature from all sensors on bus

        :return: List of temperatures or empty list on error
        """
        if not self.devices:
            print("[DS18B20] ERROR: No devices found")
            return []

        temperatures = []
        print(f"[DS18B20] Reading {len(self.devices)} sensor(s)...")

        try:
            self.ds.convert_temp()
            time.sleep_ms(750)

            for i, device in enumerate(self.devices):
                temp = self.ds.read_temp(device)
                temp_rounded = round(temp, 2)
                temperatures.append(temp_rounded)
                print(f"[DS18B20] Sensor {i}: {temp_rounded}C")

            return temperatures
        except Exception as e:
            print(f"[DS18B20] ERROR reading multiple sensors: {e}")
            return []


class KY038Sensor:
    """Driver for KY-038 noise sensor"""

    def __init__(self, analog_pin, digital_pin=None, min_db=40, max_db=120, db_calibration=65.0):
        """
        Initialize KY-038

        :param analog_pin: GPIO pin for analog output (ADC)
        :param digital_pin: GPIO pin for digital output (optional)
        :param min_db: Minimum noise level in dB (legacy, not used in peak method)
        :param max_db: Maximum noise level in dB (legacy, not used in peak method)
        :param db_calibration: Calibration offset for dB calculation (default 65.0)
        """
        # Analog input
        self.adc = ADC(Pin(analog_pin))

        # ВАЖНО ДЛЯ ESP32: Настройка диапазона напряжения
        # ATTN_11DB позволяет читать напряжение от 0 до ~3.3В (или 3.6В)
        try:
            self.adc.atten(ADC.ATTN_11DB)
        except:
            pass  # На Pico и ESP8266 этот метод не нужен/не работает так же

        # Digital input (optional)
        self.digital_pin = Pin(digital_pin, Pin.IN) if digital_pin else None

        # Calibration
        self.min_db = min_db  # Legacy
        self.max_db = max_db  # Legacy
        self.db_calibration = db_calibration
        self.adc_max = 4095  # Legacy для read()
        self.analog_pin = analog_pin

    def read_raw(self, verbose=False):
        """
        Read raw ADC value (legacy method, 12-bit)

        :param verbose: Print detailed information
        :return: ADC value 0-4095
        """
        raw = self.adc.read()
        if verbose:
            voltage = (raw / self.adc_max) * 3.3
            print(f"[KY-038] Raw ADC: {raw} / {self.adc_max} ({voltage:.2f}V)")
        return raw

    def get_loudness(self, window_ms=50):
        """
        Measure loudness by analyzing peak-to-peak amplitude over a time window
        Метод из test_quet.py

        :param window_ms: Sampling window in milliseconds (default 50ms)
        :return: Voltage amplitude (Volts)
        """
        start_time = time.ticks_ms()

        # Инициализируем мин/макс значениями наоборот
        signal_max = 0
        signal_min = 65535  # MicroPython read_u16() выдает значения 0-65535

        # Собираем выборки в течение заданного окна
        while time.ticks_diff(time.ticks_ms(), start_time) < window_ms:
            # Читаем значение (0-65535)
            sample = self.adc.read_u16()

            if sample > signal_max:
                signal_max = sample
            if sample < signal_min:
                signal_min = sample

        # Амплитуда (размах сигнала)
        peak_to_peak = signal_max - signal_min

        # Конвертация в Вольты (для 3.3В логики)
        # 65535 - это максимальное значение для 16-битного чтения
        volts = (peak_to_peak * 3.3) / 65535

        return volts

    def read_noise(self, samples=10, verbose=True, use_peak_method=True):
        """
        Read noise level in dB
        По умолчанию использует метод из test_quet.py

        :param samples: Number of measurements to average (not used in peak method)
        :param verbose: Print detailed information
        :param use_peak_method: Use peak-to-peak method (True) or legacy averaging (False)
        :return: Noise level in dB or -1 on error
        """
        try:
            if use_peak_method:
                # Метод из test_quet.py: анализ размаха сигнала с логарифмическим преобразованием
                if verbose:
                    print(f"[KY-038] Starting noise measurement (pin GPIO{self.analog_pin})...")
                    print(f"[KY-038] Using peak-to-peak analysis method")

                volts = self.get_loudness(window_ms=50)

                # Защита от log(0) и очень тихих шумов
                if volts < 0.005:
                    volts = 0.005

                # Формула дБ: 20 * log10(volts) + калибровка
                db = 20 * math.log10(volts) + self.db_calibration
                db = round(db, 1)

                if verbose:
                    print(f"[KY-038] Peak-to-peak voltage: {volts:.3f}V")
                    print(f"[KY-038] OK Noise level: {db} dB")
                    print(f"[KY-038]   Calibration offset: {self.db_calibration} dB")

                    # Noise level analysis
                    if db < 50:
                        print(f"[KY-038]   Status: Very Quiet")
                    elif db < 60:
                        print(f"[KY-038]   Status: Quiet")
                    elif db < 70:
                        print(f"[KY-038]   Status: Normal conversation")
                    elif db < 85:
                        print(f"[KY-038]   Status: Noisy")
                    elif db < 100:
                        print(f"[KY-038]   Status: Very Noisy")
                    else:
                        print(f"[KY-038]   Status: EXTREMELY LOUD")
                else:
                    print(f"[KY-038] Noise: {db} dB (Volts: {volts:.3f}V)")

                return db

            else:
                # Старый метод: усреднение ADC значений (legacy, не рекомендуется)
                if verbose:
                    print(f"[KY-038] Starting noise measurement (pin GPIO{self.analog_pin})...")
                    print(f"[KY-038] Taking {samples} samples (legacy method)...")

                values = []
                for i in range(samples):
                    value = self.adc.read()
                    values.append(value)
                    if verbose and samples <= 5:
                        print(f"[KY-038]   Sample {i+1}: {value}")
                    time.sleep_ms(10)

                # Average value
                avg_value = sum(values) / len(values)
                min_value = min(values)
                max_value = max(values)

                if verbose:
                    print(f"[KY-038] Statistics:")
                    print(f"[KY-038]   Average: {avg_value:.1f}")
                    print(f"[KY-038]   Min: {min_value}")
                    print(f"[KY-038]   Max: {max_value}")
                    print(f"[KY-038]   Range: {max_value - min_value}")

                # Convert ADC to dB (linear approximation)
                db = self.min_db + (avg_value / self.adc_max) * (self.max_db - self.min_db)
                db = round(db, 1)

                if verbose:
                    print(f"[KY-038] OK Noise level: {db} dB")
                    print(f"[KY-038]   Calibration range: {self.min_db}-{self.max_db} dB")

                    # Noise level analysis
                    if db < 50:
                        print(f"[KY-038]   Status: Very Quiet")
                    elif db < 60:
                        print(f"[KY-038]   Status: Quiet")
                    elif db < 70:
                        print(f"[KY-038]   Status: Normal conversation")
                    elif db < 85:
                        print(f"[KY-038]   Status: Noisy")
                    elif db < 100:
                        print(f"[KY-038]   Status: Very Noisy")
                    else:
                        print(f"[KY-038]   Status: EXTREMELY LOUD")
                else:
                    print(f"[KY-038] Noise: {db} dB (ADC: {avg_value:.0f})")

                return db

        except Exception as e:
            print(f"[KY-038] ERROR: {e}")
            return -1

    def is_noise_detected(self):
        """
        Check digital output (threshold)

        :return: True if noise exceeds threshold
        """
        if self.digital_pin:
            detected = self.digital_pin.value() == 0  # Usually LOW = noise detected
            print(f"[KY-038] Digital output: {'NOISE DETECTED' if detected else 'Quiet'}")
            return detected
        return False


class BatterySensor:
    """Battery charge level sensor (via voltage divider)"""

    def __init__(self, adc_pin=None, min_voltage=3.0, max_voltage=4.2):
        """
        Initialize battery sensor

        :param adc_pin: GPIO pin for voltage measurement (or None if not used)
        :param min_voltage: Minimum voltage (0%)
        :param max_voltage: Maximum voltage (100%)
        """
        self.adc = None
        self.adc_pin = adc_pin
        if adc_pin is not None:
            self.adc = ADC(Pin(adc_pin))
            self.adc.atten(ADC.ATTN_11DB)
            self.adc.width(ADC.WIDTH_12BIT)

        self.min_voltage = min_voltage
        self.max_voltage = max_voltage
        self.adc_max = 4095
        self.vref = 3.3  # ADC reference voltage

    def read_voltage(self, verbose=False):
        """
        Read battery voltage

        :param verbose: Print detailed information
        :return: Voltage in volts or -1
        """
        if not self.adc:
            if verbose:
                print("[BATTERY] ERROR: Battery sensor not configured (adc_pin is None)")
            return -1

        try:
            raw = self.adc.read()
            # Convert ADC to volts
            # IMPORTANT: Account for voltage divider coefficient!
            voltage = (raw / self.adc_max) * self.vref
            voltage_rounded = round(voltage, 2)

            if verbose:
                print(f"[BATTERY] Raw ADC: {raw} / {self.adc_max}")
                print(f"[BATTERY] Voltage: {voltage_rounded}V")

            return voltage_rounded
        except Exception as e:
            print(f"[BATTERY] ERROR reading voltage: {e}")
            return -1

    def read_percentage(self, verbose=True):
        """
        Read charge level in percentage

        :param verbose: Print detailed information
        :return: Charge level 0-100% or -1
        """
        if not self.adc:
            if verbose:
                print("[BATTERY] WARNING: Battery sensor not configured")
            return -1

        voltage = self.read_voltage(verbose=False)
        if voltage < 0:
            return -1

        # Convert voltage to percentage
        percentage = ((voltage - self.min_voltage) / (self.max_voltage - self.min_voltage)) * 100
        percentage = max(0, min(100, percentage))  # Limit 0-100
        percentage_int = int(percentage)

        if verbose:
            print(f"[BATTERY] Reading from GPIO{self.adc_pin}:")
            print(f"[BATTERY]   Voltage: {voltage}V")
            print(f"[BATTERY]   Charge: {percentage_int}%")
            print(f"[BATTERY]   Range: {self.min_voltage}V - {self.max_voltage}V")

            # Visual indicator
            bars = int(percentage / 10)
            bar_str = "#" * bars + "." * (10 - bars)
            print(f"[BATTERY]   [{bar_str}]")

            # Battery status
            if percentage_int > 80:
                print(f"[BATTERY]   Status: Excellent")
            elif percentage_int > 50:
                print(f"[BATTERY]   Status: Good")
            elif percentage_int > 20:
                print(f"[BATTERY]   Status: Low")
            elif percentage_int > 10:
                print(f"[BATTERY]   Status: Critical")
            else:
                print(f"[BATTERY]   Status: EMERGENCY")

        return percentage_int

