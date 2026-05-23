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


# ── A-weighting IIR (3 SOS-секции) ───────────────────────────────────────────
# Передаточная функция A-weighting по IEC 61672:
#   H_A(s) = K · s^4 / [(s+ω1)^2 · (s+ω2) · (s+ω3) · (s+ω4)^2]
# где ω1=2π·20.6, ω2=2π·107.7, ω3=2π·737.9, ω4=2π·12194.2 рад/с.
#
# Коэффициенты ниже получены bilinear-преобразованием для fs=32_000 Гц.
# Для других sample rate их нужно пересчитывать (или сделать таблицу).
# Финальный gain (≈1.363 в 3-й секции) выбран так, чтобы |H|=1 при 1 кГц.
#
# Каждая строка = (b0, b1, b2, a1, a2). a0 везде = 1.
_AW_SOS_32K = (
    (1.000000, -2.000000, 1.000000, -1.991930, 0.991947),  # двойной полюс 20.6 Гц
    (1.000000, -2.000000, 1.000000, -1.843956, 0.846791),  # полюса 107.7 и 737.9 Гц
    (1.363000,  0.000000, 0.000000,  0.179450, 0.008051),  # двойной полюс 12194 Гц + gain
)


def _parse_format(name):
    """
    Возвращает (i2s_format, channel_offset_bytes, channel_stride_bytes).

    Для MONO: stride = sample_size (берём каждый сэмпл подряд).
    Для STEREO: stride = sample_size*2 (L и R чередуются), offset выбирает канал.
    """
    name = (name or "MONO").upper()
    sample_size = config.I2S_BITS // 8   # 4
    if name == "MONO":
        return I2S.MONO, 0, sample_size
    if name == "STEREO_LEFT":
        return I2S.STEREO, 0, sample_size * 2
    if name == "STEREO_RIGHT":
        return I2S.STEREO, sample_size, sample_size * 2
    # неизвестное значение → fallback на MONO с предупреждением в логах
    print("[NOISE] unknown NOISE_I2S_FORMAT={!r}, falling back to MONO".format(name))
    return I2S.MONO, 0, sample_size


class NoiseSensor:
    def __init__(self):
        i2s_format, offset, stride = _parse_format(
            getattr(config, "NOISE_I2S_FORMAT", "MONO")
        )
        self._sample_offset = offset
        self._sample_stride = stride
        self._sample_size = config.I2S_BITS // 8

        self.i2s = I2S(
            config.I2S_ID,
            sck=Pin(config.INMP441_SCK_PIN),
            ws=Pin(config.INMP441_WS_PIN),
            sd=Pin(config.INMP441_SD_PIN),
            mode=I2S.RX,
            bits=config.I2S_BITS,
            format=i2s_format,
            rate=config.I2S_SAMPLE_RATE_HZ,
            ibuf=config.I2S_BUFFER_BYTES * 2,
        )
        self.buf = bytearray(config.I2S_BUFFER_BYTES)
        # Состояние A-weighting IIR: 3 секции × (x1, x2, y1, y2) = 12 float.
        # Хранится в виде flat-array, обнуляется на каждом read() — это даёт
        # "холодный старт" фильтра в начале каждого окна (200 мс), что
        # лучше, чем тянуть стейт между раздельными вызовами с deinit/init.
        self._aw_state = [0.0] * 12
        # Прогрев. После старта BCLK INMP441 выдаёт ВАЛИДНЫЕ байты сразу же,
        # но внутренний high-pass на 3 Гц устаканивается ~250–500 мс, а после
        # cold-boot переходный процесс ADC занимает ещё дольше. Без полного
        # прогрева DC-offset уплывает на сотни тысяч/миллионы единиц шкалы
        # и забивает реальный сигнал даже после программного DC-removal.
        #
        # Раньше тут было "читать до 5 раз, пока не увидим ненулевые байты" —
        # это НЕ работает: ненулевые байты появляются сразу же (сатурированные
        # −8.4 М на startup), цикл выходит после первого же readinto и
        # фактический warmup = 32 мс. Поэтому теперь читаем фиксированное
        # время из config.NOISE_WARMUP_MS (по умолчанию 2000 мс).
        warmup_ms = getattr(config, "NOISE_WARMUP_MS", 2000)
        if warmup_ms > 0:
            deadline = utime.ticks_add(utime.ticks_ms(), warmup_ms)
            while utime.ticks_diff(deadline, utime.ticks_ms()) > 0:
                try:
                    self.i2s.readinto(self.buf)
                except Exception:
                    utime.sleep_ms(10)

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
            sample_size = self._sample_size
            stride = self._sample_stride
            offset = self._sample_offset
            samples = []
            deadline = utime.ticks_add(utime.ticks_ms(), 1000)

            while len(samples) < samples_needed:
                if utime.ticks_diff(deadline, utime.ticks_ms()) <= 0:
                    break
                n = self.i2s.readinto(self.buf)
                if not n:
                    utime.sleep_ms(5)
                    continue
                # При MONO: stride==sample_size, читаем подряд.
                # При STEREO: stride==2*sample_size, offset выбирает канал.
                for i in range(offset, n - sample_size + 1, stride):
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

            # 2) Junk-мусор у INMP441 при unconnected/плавающем L/R прилетает
            # ~±2^22 по неактивному каналу. Порог из config.NOISE_JUNK_THRESHOLD
            # (по умолчанию 2^18). С A-weighting мы НЕ выкидываем мусорные
            # сэмплы (это сломает стейт IIR-фильтра), а зануляем их — фильтр
            # видит "тишину" в этих местах. Без A-weighting — старая логика
            # с накоплением только чистых.
            junk_threshold = getattr(config, "NOISE_JUNK_THRESHOLD", 1 << 18)
            a_weighting_on = getattr(config, "NOISE_A_WEIGHTING", False)
            sum_sq = 0
            clean_count = 0

            if a_weighting_on:
                # Обнуляем стейт фильтра на каждое окно — иначе хвост от
                # прошлого вызова с другими условиями (или из __init__) даст
                # transient в начале нынешнего расчёта.
                for i in range(12):
                    self._aw_state[i] = 0.0
                st = self._aw_state
                # Развёрнутые коэффициенты SOS — обращение по имени быстрее
                # индексации tuple of tuples в горячем цикле MicroPython.
                b00, b01, b02, a01, a02 = _AW_SOS_32K[0]
                b10, b11, b12, a11, a12 = _AW_SOS_32K[1]
                b20, b21, b22, a21, a22 = _AW_SOS_32K[2]

                # "Прогрев" фильтра — прогоняем первые ~50 мс сэмплов через
                # IIR, не учитывая в RMS. Это нужно, чтобы фильтр устаканился
                # после reset стейта (иначе первые сэмплы дают transient).
                aw_warmup_n = min(count, config.I2S_SAMPLE_RATE_HZ // 20)  # 50 мс

                for idx in range(count):
                    s = samples[idx]
                    d = s - mean
                    if abs(d) >= junk_threshold:
                        d = 0.0  # мусор → подаём в фильтр как тишину
                    else:
                        d = float(d)
                    # Секция 1
                    x1 = st[0]; x2 = st[1]; y1 = st[2]; y2 = st[3]
                    y = b00*d + b01*x1 + b02*x2 - a01*y1 - a02*y2
                    st[1] = x1; st[0] = d; st[3] = y1; st[2] = y
                    # Секция 2
                    x1 = st[4]; x2 = st[5]; y1 = st[6]; y2 = st[7]
                    y2_in = y
                    y = b10*y2_in + b11*x1 + b12*x2 - a11*y1 - a12*y2
                    st[5] = x1; st[4] = y2_in; st[7] = y1; st[6] = y
                    # Секция 3
                    x1 = st[8]; x2 = st[9]; y1 = st[10]; y2 = st[11]
                    y3_in = y
                    y = b20*y3_in + b21*x1 + b22*x2 - a21*y1 - a22*y2
                    st[9] = x1; st[8] = y3_in; st[11] = y1; st[10] = y

                    if idx >= aw_warmup_n:
                        sum_sq += y * y
                        clean_count += 1
            else:
                for s in samples:
                    d = s - mean
                    if abs(d) < junk_threshold:
                        sum_sq += d * d
                        clean_count += 1

            # Если совсем мало чистых — мик помер или L/R-пин висит в воздухе.
            # Возвращаем -1.0, чтобы сервер пометил noise_read_error.
            if clean_count < 32:
                _log("too much junk ({}/{} clean)".format(clean_count, count))
                return -1.0
            mean_sq = sum_sq / clean_count

            if config.DEBUG:
                peak_all = 0
                peak_clean = 0
                sat_pos = 0
                sat_neg = 0
                for s in samples:
                    d = abs(s - mean)
                    if d > peak_all:
                        peak_all = d
                    if d < junk_threshold and d > peak_clean:
                        peak_clean = d
                    if s >= 8_300_000:
                        sat_pos += 1
                    elif s <= -8_300_000:
                        sat_neg += 1
                # peak_all ≈ 2^23 → шина I2S саturирует (плохо).
                # peak_clean → реальный пик "ниже фильтра" (для калибровки).
                # sat_pos/sat_neg → сколько сэмплов уперлось в края 24-битной шкалы.
                _log("clean={}/{} sat+={} sat-={} dc={:.0f} peak_all={} peak_clean={} rms={:.0f} thr={} fmt={} A={}".format(
                    clean_count, count, sat_pos, sat_neg, mean,
                    peak_all, peak_clean, math.sqrt(mean_sq), junk_threshold,
                    getattr(config, "NOISE_I2S_FORMAT", "MONO"),
                    "on" if a_weighting_on else "off"))

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

    def diagnose(self, n_samples=16, warmup_ms=2000):
        """
        Однократный дамп сырого I2S-буфера для проверки железа.

        Запусти из REPL:
            from noise import NoiseSensor
            NoiseSensor().diagnose()
        или включи config.NOISE_DIAG_ON_BOOT=True в config.py — main.py
        вызовет это автоматически перед основным циклом.

        warmup_ms: сколько миллисекунд читать и выбрасывать буфера перед
        дампом. INMP441 после старта BCLK садится в normal mode ~64 ms,
        а внутренний high-pass на 3 Гц устаканивается секундами; без
        прогрева увидишь огромный отрицательный DC-bias, который к мику
        отношения не имеет — он просто ещё не вышел из переходного.

        Как читать:
          - Живой мик в тихой комнате после прогрева: |raw>>8| < ~2000,
            mean около 0, min/max симметричны и небольшие. Если ткнуть
            колонкой — min/max сразу прыгают в сотни тысяч.
          - SD/L/R в воздухе, неверная распиновка, мёртвый мик:
            значения скачут по всему диапазону ±2^23 (clipping),
            mean гуляет в сотнях тысяч/миллионах, min/max упираются
            в ±8_388_607 — это и есть «нет аудио, есть мусор».
          - Постоянные 0 или -1: SD не подключен/нет питания.
        """
        # Прогрев — выбрасываем буфера, чтобы мик прошёл startup-transient.
        if warmup_ms > 0:
            print("[NOISE-DIAG] прогрев {} мс (выбрасываем стартовые буфера)...".format(warmup_ms))
            deadline = utime.ticks_add(utime.ticks_ms(), warmup_ms)
            drained = 0
            while utime.ticks_diff(deadline, utime.ticks_ms()) > 0:
                try:
                    self.i2s.readinto(self.buf)
                    drained += 1
                except Exception:
                    pass
            print("[NOISE-DIAG] дропнули {} буферов".format(drained))

        n = self.i2s.readinto(self.buf)
        if not n:
            print("[NOISE-DIAG] I2S вернул 0 байт — проверь питание/пины I2S.")
            return
        print("[NOISE-DIAG] readinto returned {} bytes (fmt={})".format(
            n, getattr(config, "NOISE_I2S_FORMAT", "MONO")))
        sample_size = self._sample_size
        stride = self._sample_stride
        offset = self._sample_offset

        # При STEREO дампим первые сэмплы обоих каналов, чтобы было видно,
        # в каком из них реально что-то живое.
        if stride != sample_size:
            print("[NOISE-DIAG] первые {} сэмплов raw (L | R):".format(n_samples))
            for k in range(min(n_samples, (n // stride))):
                base = k * stride
                rawL = struct.unpack_from("<i", self.buf, base)[0]
                rawR = struct.unpack_from("<i", self.buf, base + sample_size)[0]
                print("  k={:3d} L raw=0x{:08x} >>8={:+9d}  R raw=0x{:08x} >>8={:+9d}".format(
                    k, rawL & 0xFFFFFFFF, rawL >> 8,
                    rawR & 0xFFFFFFFF, rawR >> 8))
        else:
            print("[NOISE-DIAG] первые {} сэмплов (raw 32-bit и >>8 24-bit):".format(n_samples))
            for i in range(0, min(n_samples * sample_size, n), sample_size):
                raw = struct.unpack_from("<i", self.buf, i)[0]
                print("  off={:3d} raw=0x{:08x} ({:+13d}) >>8={:+9d}".format(
                    i, raw & 0xFFFFFFFF, raw, raw >> 8))

        min_v = 1 << 30
        max_v = -(1 << 30)
        sum_v = 0
        count = 0
        sat_neg = 0
        sat_pos = 0
        for i in range(offset, n - sample_size + 1, stride):
            raw = struct.unpack_from("<i", self.buf, i)[0]
            s = raw >> 8
            if s < min_v:
                min_v = s
            if s > max_v:
                max_v = s
            sum_v += s
            count += 1
            if s <= -8_300_000:
                sat_neg += 1
            if s >= 8_300_000:
                sat_pos += 1
        mean = sum_v / max(count, 1)
        print("[NOISE-DIAG] весь буфер: count={} min={} max={} mean={:.0f}".format(
            count, min_v, max_v, mean))
        print("[NOISE-DIAG] клиппинг: sat_neg(<-8.3M)={} sat_pos(>+8.3M)={}".format(
            sat_neg, sat_pos))
        # У живого мика после прогрева DC-bias должен быть в пределах ±5_000.
        # Всё что больше — либо мик ещё не устаканился (мало warmup), либо чип
        # повреждён, либо bit alignment не тот.
        fmt_now = getattr(config, "NOISE_I2S_FORMAT", "MONO")
        if sat_neg + sat_pos > count * 0.01:
            print("[NOISE-DIAG] ВЕРДИКТ: клиппинг в шкале → читаем не тот канал ИЛИ железо.")
            print("[NOISE-DIAG] Сначала ПРОСТЫЕ софт-попытки (без перепайки):")
            if fmt_now == "MONO":
                print("  A) Поставь NOISE_I2S_FORMAT=\"STEREO_RIGHT\" в config.py и")
                print("     перезагрузись. Если у тебя L/R мика на VDD/в воздухе с")
                print("     pull-up — мик кладёт данные в RIGHT-слот, MONO их не")
                print("     видит, и шина бьёт мусор. STEREO_RIGHT возьмёт правильно.")
            elif fmt_now == "STEREO_RIGHT":
                print("  A) Уже STEREO_RIGHT. Попробуй STEREO_LEFT и MONO.")
            else:
                print("  A) Уже {} — поменяй на другой вариант (MONO/STEREO_RIGHT/STEREO_LEFT).".format(fmt_now))
            print("  B) Снизь I2S_SAMPLE_RATE_HZ до 16_000 (вернёшь BCLK ≈1 МГц).")
            print("     INMP441 не стартует ниже 1.024 МГц, но 1 МГц ровно — ок.")
            print("[NOISE-DIAG] Если не помогает — это уже железо/распиновка:")
            print("  1) L/R-пин INMP441 → GND (для MONO=Left). Без этого мик")
            print("     сыпет рандом по неактивному каналу.")
            print("  2) VDD INMP441 = 3.3 В (НЕ 5 В — макс. 3.6, иначе чип")
            print("     можно угробить, и он застрянет у одного из рейлов).")
            print("  3) BCLK/WS не перепутаны: SCK=GPIO17 (~2 МГц),")
            print("     WS=GPIO18 (~32 кГц).")
            print("  4) SD-пин INMP441 → GPIO21 (а не на L/R/GND).")
            print("  5) Провода I2S < 10 см и желательно с общим GND рядом.")
        elif abs(mean) > 100_000:
            print("[NOISE-DIAG] ВЕРДИКТ: огромный DC-bias ({:.0f}) после прогрева.".format(mean))
            print("[NOISE-DIAG] У исправного INMP441 после старта BCLK через ~1с")
            print("[NOISE-DIAG] DC должен быть |mean|<5_000. Этого нет → варианты:")
            print("  1) L/R-пин в воздухе — подтяни к GND и повтори.")
            print("  2) VDD на мик > 3.6 В когда-то подавалось → чип повреждён,")
            print("     меняй модуль. Замер VDD мультиметром обязательно.")
            print("  3) Bit alignment кривой — попробуй I2S_SAMPLE_RATE_HZ=44100")
            print("     или другой format (поэкспериментируй с STEREO).")
        elif abs(mean) < 5_000 and max_v - min_v < 10_000:
            print("[NOISE-DIAG] ВЕРДИКТ: похоже на тишину/нормальный noise floor.")
            print("[NOISE-DIAG] Постучи/свистни рядом — min/max должны прыгнуть.")
        else:
            print("[NOISE-DIAG] ВЕРДИКТ: данные смешанные — возможно L/R floating")
            print("[NOISE-DIAG] или слишком короткий warmup. Подтяни L/R к GND,")
            print("[NOISE-DIAG] увеличь warmup_ms до 5000 и запусти ещё раз.")
