# -*- coding: utf-8 -*-
"""
noise.py — Чтение уровня шума с INMP441 (I2S MEMS-микрофон).

INMP441 кладёт 24-битный знаковый сэмпл в верхние биты 32-битного слова I2S.
Алгоритм: окно ~NOISE_WINDOW_MS → RMS → пересчёт в dB SPL по калибровке.

Калибровка по даташиту: чувствительность -26 dBFS при 94 dB SPL (1 кГц).
Соответствует смещению ~120 для пересчёта dBFS → dB SPL. Точная калибровка
делается под конкретный экземпляр (NOISE_DB_OFFSET в config.py).
"""

import math
import struct
import utime
from machine import I2S, Pin

import config


def _log(msg):
    if config.DEBUG:
        print("[NOISE]", msg)


class NoiseSensor:
    def __init__(self):
        self.i2s = I2S(
            config.I2S_ID,
            sck=Pin(config.INMP441_SCK_PIN),
            ws=Pin(config.INMP441_WS_PIN),
            sd=Pin(config.INMP441_SD_PIN),
            mode=I2S.RX,
            bits=config.I2S_BITS,
            format=I2S.MONO,
            rate=config.I2S_SAMPLE_RATE_HZ,
            ibuf=config.I2S_BUFFER_BYTES * 2,
        )
        self.buf = bytearray(config.I2S_BUFFER_BYTES)
        # Прогрев — первый кадр после init часто содержит мусор / DC offset
        try:
            self.i2s.readinto(self.buf)
        except Exception:
            pass

    def read(self):
        """
        Возвращает уровень шума в dB SPL (float, 1 знак),
        либо -1.0 при ошибке.
        """
        try:
            samples_needed = (config.I2S_SAMPLE_RATE_HZ * config.NOISE_WINDOW_MS) // 1000
            sum_sq = 0
            count = 0
            sample_size = config.I2S_BITS // 8   # = 4 для 32-битного слова
            deadline = utime.ticks_add(utime.ticks_ms(), 1000)

            while count < samples_needed:
                if utime.ticks_diff(deadline, utime.ticks_ms()) <= 0:
                    break
                n = self.i2s.readinto(self.buf)
                if not n:
                    utime.sleep_ms(5)
                    continue
                for i in range(0, n, sample_size):
                    raw = struct.unpack_from("<i", self.buf, i)[0]
                    # Сдвигаем 24 бита данных из верхних разрядов вниз
                    sample = raw >> 8
                    sum_sq += sample * sample
                    count += 1
                    if count >= samples_needed:
                        break

            if count == 0:
                _log("No samples read")
                return -1.0

            mean_sq = sum_sq / count
            if mean_sq < 1:
                # Полная тишина → бесконечно отрицательный dB. Возвращаем нижний разумный.
                return 0.0

            rms = math.sqrt(mean_sq)
            full_scale = float(1 << 23)        # 24-битный знаковый максимум
            dbfs = 20.0 * math.log10(rms / full_scale)
            db_spl = dbfs + config.NOISE_DB_OFFSET
            # Клипуем разумный диапазон
            if db_spl < 0:
                db_spl = 0.0
            return round(db_spl, 1)
        except Exception as e:
            _log("Read error: {}".format(e))
            return -1.0

    def deinit(self):
        try:
            self.i2s.deinit()
        except Exception:
            pass
