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
        # Прогрев — после deepsleep I2S может выдавать нули несколько кадров.
        # Читаем до 5 раз, пока не увидим ненулевые байты.
        for _ in range(5):
            try:
                n = self.i2s.readinto(self.buf)
                if n and any(self.buf[:min(64, n)]):
                    break
            except Exception:
                pass
            utime.sleep_ms(50)

    def read(self):
        """
        Возвращает уровень шума в dB SPL (float, 1 знак),
        либо -1.0 при ошибке.

        Алгоритм:
          1. Собираем окно сэмплов (NOISE_WINDOW_MS).
          2. Вычитаем DC-смещение (среднее) — INMP441 даёт постоянную составляющую,
             без её удаления RMS улетает в стратосферу и даёт фиктивные ~110+ dB.
          3. RMS → dBFS → dB SPL по калибровке.
        """
        try:
            samples_needed = (config.I2S_SAMPLE_RATE_HZ * config.NOISE_WINDOW_MS) // 1000
            sample_size = config.I2S_BITS // 8   # = 4 для 32-битного слова
            samples = []
            deadline = utime.ticks_add(utime.ticks_ms(), 1000)

            while len(samples) < samples_needed:
                if utime.ticks_diff(deadline, utime.ticks_ms()) <= 0:
                    break
                n = self.i2s.readinto(self.buf)
                if not n:
                    utime.sleep_ms(5)
                    continue
                for i in range(0, n, sample_size):
                    raw = struct.unpack_from("<i", self.buf, i)[0]
                    samples.append(raw >> 8)
                    if len(samples) >= samples_needed:
                        break

            count = len(samples)
            if count == 0:
                _log("No samples read")
                return -1.0

            # 1) DC offset
            mean = sum(samples) / count

            # 2) Фильтруем "мусорные" сэмплы — у этого мика половина буфера
            # бьётся в ±2^22 из-за неконфигурируемого LR-канала. Считаем RMS
            # только по реальным (которые меньше JUNK_THRESHOLD от dc).
            JUNK_THRESHOLD = 1 << 18    # 256 КБ — отсекает явный мусор, реальный звук до ~95 dB SPL проходит
            sum_sq = 0
            clean_count = 0
            for s in samples:
                d = s - mean
                if abs(d) < JUNK_THRESHOLD:
                    sum_sq += d * d
                    clean_count += 1

            # Если совсем мало чистых — мик помер, возвращаем error
            if clean_count < 32:
                _log("too much junk ({}/{} clean)".format(clean_count, count))
                return -1.0
            mean_sq = sum_sq / clean_count

            if config.DEBUG:
                peak_all = max(abs(s - mean) for s in samples)
                _log("clean={}/{} dc={:.0f} peak_all={} rms={:.0f}".format(
                    clean_count, count, mean, peak_all, math.sqrt(mean_sq)))

            if mean_sq < 1:
                return 0.0

            rms = math.sqrt(mean_sq)
            full_scale = float(1 << 23)
            dbfs = 20.0 * math.log10(rms / full_scale)
            db_spl = dbfs + config.NOISE_DB_OFFSET
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
