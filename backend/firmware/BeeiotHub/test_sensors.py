# -*- coding: utf-8 -*-
"""
Test script for autonomous sensor checking WITHOUT server
Run this file to test sensors on ESP32
"""

import time
import sys

print("\n" + "="*60)
print("           SENSOR TESTING WITHOUT SERVER")
print("="*60 + "\n")

print("[DEBUG] Starting module import...")

try:
    print("[DEBUG] Importing sensors...")
    from sensors import DS18B20Sensor, KY038Sensor, BatterySensor
    print("[DEBUG] OK sensors imported")
except Exception as e:
    print(f"[DEBUG] ERROR importing sensors: {e}")
    sys.print_exception(e)
    raise

try:
    print("[DEBUG] Importing config...")
    from config import *
    print("[DEBUG] OK config imported")
except Exception as e:
    print(f"[DEBUG] ERROR importing config: {e}")
    sys.print_exception(e)
    raise

print("[DEBUG] All modules successfully imported!\n")

# Initialize sensors
print("[INIT] Initializing sensors...\n")

# 1. DS18B20 - Temperature sensor
print("-" * 60)
print("1. DS18B20 - TEMPERATURE SENSOR")
print("-" * 60)
print(f"[DEBUG] Trying to initialize on GPIO {DS18B20_PIN}...")
try:
    temp_sensor = DS18B20Sensor(DS18B20_PIN)
    print(f"OK DS18B20 initialized on GPIO {DS18B20_PIN}")
    print(f"  Devices found: {len(temp_sensor.devices)}")
except Exception as e:
    temp_sensor = None
    print(f"ERROR initializing DS18B20: {e}")
    sys.print_exception(e)

print()

# 2. KY-038 - Noise sensor
print("-" * 60)
print("2. KY-038 - NOISE LEVEL SENSOR")
print("-" * 60)
print(f"[DEBUG] Trying to initialize on GPIO {KY038_ANALOG_PIN}...")
try:
    noise_sensor = KY038Sensor(
        KY038_ANALOG_PIN,
        KY038_DIGITAL_PIN,
        NOISE_MIN_DB,
        NOISE_MAX_DB,
        DB_CALIBRATION  # Добавляем новый параметр калибровки
    )
    print(f"OK KY-038 initialized")
    print(f"  Analog pin: GPIO {KY038_ANALOG_PIN}")
    print(f"  Digital pin: GPIO {KY038_DIGITAL_PIN}")
    print(f"  Range: {NOISE_MIN_DB}-{NOISE_MAX_DB} dB")
    print(f"  Calibration: {DB_CALIBRATION} dB")
except Exception as e:
    noise_sensor = None
    print(f"ERROR initializing KY-038: {e}")
    sys.print_exception(e)

print()

# 3. Battery sensor (if configured)
print("-" * 60)
print("3. BATTERY SENSOR")
print("-" * 60)
print(f"[DEBUG] BATTERY_ADC_PIN = {BATTERY_ADC_PIN}")
try:
    battery_sensor = BatterySensor(
        BATTERY_ADC_PIN,
        BATTERY_MIN_VOLTAGE,
        BATTERY_MAX_VOLTAGE
    )
    if BATTERY_ADC_PIN is not None:
        print(f"OK Battery sensor initialized on GPIO {BATTERY_ADC_PIN}")
        print(f"  Range: {BATTERY_MIN_VOLTAGE}V - {BATTERY_MAX_VOLTAGE}V")
    else:
        print("WARNING Battery sensor not configured (BATTERY_ADC_PIN = None)")
        battery_sensor = None
except Exception as e:
    battery_sensor = None
    print(f"ERROR initializing battery sensor: {e}")
    sys.print_exception(e)

print("\n" + "="*60)
print("           STARTING CONTINUOUS MONITORING")
print("="*60)
print("Press Ctrl+C to stop\n")

# Measurement counter
measurement_count = 0

try:
    while True:
        measurement_count += 1
        timestamp = time.time()

        print(f"\n{'='*60}")
        print(f"MEASUREMENT #{measurement_count} | Time: {int(timestamp)}")
        print(f"{'='*60}\n")

        # === TEMPERATURE ===
        print("TEMPERATURE:")
        if temp_sensor:
            try:
                temperature = temp_sensor.read_temperature()
                if temperature != -1:
                    print(f"   OK Temperature: {temperature}C")

                    # Temperature analysis
                    if temperature < 10:
                        print("   WARNING Low temperature (< 10C)")
                    elif temperature > 40:
                        print("   WARNING High temperature (> 40C)")
                    else:
                        print("   OK Temperature in normal range")
                else:
                    print("   ERROR Reading temperature")
            except Exception as e:
                print(f"   ERROR: {e}")
                sys.print_exception(e)
        else:
            print("   ERROR Sensor not initialized")

        print()

        # === NOISE ===
        print("NOISE LEVEL:")
        if noise_sensor:
            try:
                # Read raw ADC value
                raw_value = noise_sensor.read_raw()
                print(f"   Raw ADC value: {raw_value} (0-4095)")

                # Read level in dB
                noise_level = noise_sensor.read_noise(samples=10, verbose=False)
                if noise_level != -1:
                    print(f"   OK Noise level: {noise_level} dB")

                    # Noise level analysis
                    if noise_level < 50:
                        print("   OK Very quiet")
                    elif noise_level < 70:
                        print("   OK Normal level")
                    elif noise_level < 90:
                        print("   WARNING Noisy")
                    else:
                        print("   WARNING Very noisy!")

                    # Check digital output (threshold)
                    if noise_sensor.digital_pin:
                        is_detected = noise_sensor.is_noise_detected()
                        print(f"   Digital output: {'NOISE DETECTED' if is_detected else 'Quiet'}")
                else:
                    print("   ERROR Reading noise level")
            except Exception as e:
                print(f"   ERROR: {e}")
                sys.print_exception(e)
        else:
            print("   ERROR Sensor not initialized")

        print()

        # === BATTERY ===
        print("BATTERY:")
        if battery_sensor and BATTERY_ADC_PIN is not None:
            try:
                voltage = battery_sensor.read_voltage()
                percentage = battery_sensor.read_percentage()

                if voltage != -1:
                    print(f"   OK Voltage: {voltage}V")
                if percentage != -1:
                    print(f"   OK Charge level: {percentage}%")

                    # Charge indicator
                    if percentage > 80:
                        bar = "#########."
                        status = "Excellent"
                    elif percentage > 60:
                        bar = "#######..."
                        status = "Good"
                    elif percentage > 40:
                        bar = "#####....."
                        status = "Medium"
                    elif percentage > 20:
                        bar = "###......."
                        status = "WARNING Low"
                    else:
                        bar = "#........."
                        status = "WARNING CRITICAL!"

                    print(f"   [{bar}] {status}")

                if voltage == -1 and percentage == -1:
                    print("   ERROR Reading battery data")
            except Exception as e:
                print(f"   ERROR: {e}")
                sys.print_exception(e)
        else:
            print("   WARNING Sensor not configured")

        print()

        # === SUMMARY ===
        print("SUMMARY:")
        data_summary = []
        if temp_sensor:
            try:
                temp = temp_sensor.read_temperature(verbose=False)
                if temp != -1:
                    data_summary.append(f"T={temp}C")
            except:
                pass
        if noise_sensor:
            try:
                noise = noise_sensor.read_noise(samples=5, verbose=False)
                if noise != -1:
                    data_summary.append(f"N={noise}dB")
            except:
                pass
        if battery_sensor and BATTERY_ADC_PIN is not None:
            try:
                batt = battery_sensor.read_percentage(verbose=False)
                if batt != -1:
                    data_summary.append(f"B={batt}%")
            except:
                pass

        if data_summary:
            print(f"   {' | '.join(data_summary)}")
        else:
            print("   No data")

        print(f"\n{'='*60}")

        # Pause between measurements
        print("Next measurement in 5 seconds...")
        time.sleep(5)

except KeyboardInterrupt:
    print("\n\n" + "="*60)
    print("           TESTING STOPPED")
    print("="*60)
    print(f"\nTotal measurements: {measurement_count}")
    print("\nTest results:")
    print(f"  - DS18B20 (temperature): {'OK Works' if temp_sensor else 'ERROR Not working'}")
    print(f"  - KY-038 (noise): {'OK Works' if noise_sensor else 'ERROR Not working'}")
    print(f"  - Battery sensor: {'OK Works' if (battery_sensor and BATTERY_ADC_PIN) else 'WARNING Not configured'}")
    print("\nTesting completed!\n")

except Exception as e:
    print(f"\n\nERROR CRITICAL: {e}")
    sys.print_exception(e)
    print("Testing interrupted\n")
