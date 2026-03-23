from machine import ADC, Pin
import time
import math

# --- НАСТРОЙКИ ---

# Выберите правильный пин для вашей платы:
# Для ESP32 обычно GPIO 32, 33, 34, 35 (например, Pin(34))
# Для Raspberry Pi Pico это 26, 27 или 28
adc_pin_nr = 7

# Калибровка (подбираем опытным путем, как и на Arduino)
db_calibration = 65.0

# Инициализация АЦП
adc = ADC(Pin(adc_pin_nr))

# ВАЖНО ДЛЯ ESP32: Настройка диапазона напряжения
# ATTN_11DB позволяет читать напряжение от 0 до ~3.3В (или 3.6В)
# Если у вас Raspberry Pi Pico, эту строчку нужно удалить или закомментировать
try:
    adc.atten(ADC.ATTN_11DB)
except:
    pass  # На Pico и ESP8266 этот метод не нужен/не работает так же


def get_loudness():
    # Окно выборки 50 мс
    window_ms = 50
    start_time = time.ticks_ms()

    # Инициализируем мин/макс значениями наоборот
    signal_max = 0
    signal_min = 65535  # MicroPython read_u16() выдает значения 0-65535

    while time.ticks_diff(time.ticks_ms(), start_time) < window_ms:
        # Читаем значение (0-65535)
        sample = adc.read_u16()

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


while True:
    volts = get_loudness()

    # Защита от log(0) и очень тихих шумов
    if volts < 0.005:
        volts = 0.005

    # Формула дБ
    db = 20 * math.log10(volts) + db_calibration

    print("Volts: {:.3f} | dB: {:.1f}".format(volts, db))

    time.sleep(0.1)