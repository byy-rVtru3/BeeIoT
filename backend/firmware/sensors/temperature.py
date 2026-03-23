# -*- coding: utf-8 -*-
"""
sensors/temperature.py — Драйвер DS18B20.

Использует встроенные модули onewire и ds18x20 (MicroPython).
При любой ошибке возвращает -1.0, не выбрасывает исключения.
"""

import onewire
import ds18x20
import machine
import utime

import config


def _log(msg):
    if config.DEBUG:
        print("[TEMP]", msg)


class TemperatureSensor:
    """
    Драйвер датчика температуры DS18B20 на 1-Wire шине.

    Пример использования:
        sensor = TemperatureSensor(config.DS18B20_PIN)
        temp = sensor.read()   # 23.5 или -1.0 при ошибке
    """

    ERROR = -1.0

    def __init__(self, pin: int):
        """
        Инициализирует 1-Wire шину и ищет DS18B20.

        :param pin: Номер GPIO пина (из config.DS18B20_PIN)
        """
        self._rom = None
        try:
            ow = onewire.OneWire(machine.Pin(pin))
            self._ds = ds18x20.DS18X20(ow)
            roms = self._ds.scan()
            if roms:
                self._rom = roms[0]
                _log("Найден датчик: {}".format(self._rom))
            else:
                _log("ОШИБКА: датчик DS18B20 не найден на пине {}".format(pin))
        except Exception as e:
            _log("ОШИБКА инициализации: {}".format(e))
            self._ds = None
            self._rom = None

    def read(self) -> float:
        """
        Читает температуру с датчика DS18B20.

        Запускает конвертацию, ждёт TEMP_MEASURE_WAIT_MS мс, считывает результат.

        :return: Температура в °C (float) или -1.0 при любой ошибке.
        """
        if self._ds is None or self._rom is None:
            _log("Датчик не инициализирован, возвращаю -1")
            return self.ERROR

        try:
            self._ds.convert_temp()
            utime.sleep_ms(config.TEMP_MEASURE_WAIT_MS)
            temp = self._ds.read_temp(self._rom)
            _log("Температура: {:.2f}°C".format(temp))
            return float(temp)
        except Exception as e:
            _log("ОШИБКА чтения: {}".format(e))
            return self.ERROR
