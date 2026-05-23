# -*- coding: utf-8 -*-
"""
temperature.py — Чтение DS18B20 (1-Wire) на ESP32-S3.

Pull-up 4.7–10 кОм между DQ и 3V3 обязателен (на готовых водонепроницаемых
зондах он часто уже стоит — проверять мультиметром).
"""

import utime
import onewire
import ds18x20
from machine import Pin

import config


def _log(msg):
    if config.DEBUG:
        print("[TEMP]", msg)


class TemperatureSensor:
    def __init__(self, pin):
        self.ow = onewire.OneWire(Pin(pin))
        self.ds = ds18x20.DS18X20(self.ow)
        self.roms = []
        try:
            self.roms = self.ds.scan()
        except Exception as e:
            _log("Scan error: {}".format(e))

    def read(self):
        """
        Возвращает температуру в °C (float, округлено до 2 знаков),
        либо -1.0 при ошибке.
        """
        if not self.roms:
            _log("No DS18B20 on bus")
            return -1.0
        try:
            self.ds.convert_temp()
            utime.sleep_ms(config.TEMP_CONVERT_MS)
            t = self.ds.read_temp(self.roms[0])
            if t is None:
                return -1.0
            # DS18B20 при ошибке коммуникации может вернуть 85.0 — это reset value.
            # Проверяем явно: если температура ровно 85.0 и до этого не было корректного чтения,
            # считаем это ошибкой. Но точное определение требует контекста, поэтому просто логируем.
            if t == 85.0:
                _log("Got 85.0 (suspicious — possible bus error)")
            return round(t, 2)
        except Exception as e:
            _log("Read error: {}".format(e))
            return -1.0
