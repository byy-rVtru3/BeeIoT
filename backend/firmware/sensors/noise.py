# -*- coding: utf-8 -*-
"""
sensors/noise.py — Драйвер датчика шума KY-038.

Использует машинный ADC (MicroPython). Метод peak-to-peak.
При любой ошибке возвращает -1.0, не выбрасывает исключения.
"""

import machine
import utime
import math

import config


def _log(msg):
    if config.DEBUG:
        print("[NOISE]", msg)


class NoiseSensor:
    """
    Драйвер датчика шума KY-038 (аналоговый выход → ADC ESP32).

    Алгоритм:
        1. Снимает NOISE_SAMPLES замеров с интервалом NOISE_SAMPLE_MS мс
        2. Вычисляет peak-to-peak разницу (max - min)
        3. Конвертирует в dB: db = 20 * log10(volts + epsilon) + DB_CALIBRATION

    Пример использования:
        sensor = NoiseSensor(config.KY038_ADC_PIN)
        db = sensor.read()   # 45.2 или -1.0 при ошибке
    """

    ERROR = -1.0
    _EPSILON = 1e-9    # Защита от log10(0)

    def __init__(self, pin: int):
        """
        Инициализирует ADC на указанном пине.

        :param pin: Номер GPIO пина с ADC-поддержкой (из config.KY038_ADC_PIN)
        """
        self._adc = None
        try:
            self._adc = machine.ADC(machine.Pin(pin))
            # Полная ширина: 0–3.3В → 0–4095 (12 бит)
            self._adc.atten(machine.ADC.ATTN_11DB)
            self._adc.width(machine.ADC.WIDTH_12BIT)
            _log("ADC инициализирован на пине GPIO{}".format(pin))
        except Exception as e:
            _log("ОШИБКА инициализации ADC: {}".format(e))
            self._adc = None

    def read(self) -> float:
        """
        Измеряет уровень шума методом peak-to-peak.

        :return: Уровень шума в dB (float) или -1.0 при любой ошибке.
        """
        if self._adc is None:
            _log("ADC не инициализирован, возвращаю -1")
            return self.ERROR

        try:
            return self._measure()
        except Exception as e:
            _log("ОШИБКА измерения: {}".format(e))
            return self.ERROR

    def _measure(self) -> float:
        """Выполняет серию замеров и вычисляет уровень шума."""
        min_val = config.ADC_MAX_VALUE
        max_val = 0

        for _ in range(config.NOISE_SAMPLES):
            val = self._adc.read()
            if val < min_val:
                min_val = val
            if val > max_val:
                max_val = val
            utime.sleep_ms(config.NOISE_SAMPLE_MS)

        peak = max_val - min_val
        db = self._adc_to_db(peak)
        _log("Peak-to-peak: {}, уровень: {:.2f} dB".format(peak, db))
        return db

    def _adc_to_db(self, peak_raw: int) -> float:
        """
        Конвертирует пиковое значение ADC в уровень звука dB.

        Формула: db = 20 * log10(volts + epsilon) + DB_CALIBRATION

        :param peak_raw: Пиковое значение ADC (0 – ADC_MAX_VALUE)
        :return: Уровень в dB
        """
        volts = (peak_raw / config.ADC_MAX_VALUE) * config.ADC_VREF
        db = 20.0 * math.log10(volts + self._EPSILON) + config.DB_CALIBRATION
        return float(db)
