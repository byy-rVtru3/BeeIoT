# -*- coding: utf-8 -*-
"""
ring_buffer.py — буфер для оффлайн-хранения данных.

Бэкенды (config.BUFFER_BACKEND):
  "ram"   — только в RAM (data lost on reset, но не насилует флеш/SD)
  "sd"    — SD-карта по SPI
  "flash" — внутренний flash (FAT)
  "auto"  — пытаемся SD, при неудаче → flash

Файловые режимы (sd/flash/auto): RAM-кэш сверху, реальная запись на диск
батчами раз в BUFFER_FLUSH_MIN_MS либо по BUFFER_FLUSH_MAX_RECS записей.
"""

import os
import utime
import ujson

import config


def _log(msg):
    if config.DEBUG:
        print("[BUF]", msg)


def _ensure_dir(path):
    parts = path.rsplit("/", 1)
    if len(parts) == 2 and parts[0]:
        try:
            os.stat(parts[0])
        except OSError:
            try:
                os.mkdir(parts[0])
            except OSError:
                pass


def _mount_sd():
    """Монтирует SD-карту по SPI. Возвращает True/False."""
    try:
        os.stat(config.SD_MOUNT)
        return True  # уже примонтирована
    except OSError:
        pass
    try:
        import machine
        import sdcard
        spi = machine.SPI(
            config.SD_SPI_ID,
            baudrate=1_000_000,
            polarity=0, phase=0,
            sck=machine.Pin(config.SD_SCK_PIN),
            mosi=machine.Pin(config.SD_MOSI_PIN),
            miso=machine.Pin(config.SD_MISO_PIN),
        )
        cs = machine.Pin(config.SD_CS_PIN, machine.Pin.OUT)
        sd = sdcard.SDCard(spi, cs)
        os.mount(sd, config.SD_MOUNT)
        _log("SD mounted at {}".format(config.SD_MOUNT))
        return True
    except Exception as e:
        _log("SD mount failed: {}".format(e))
        return False


class RingBuffer:
    """
    Универсальный буфер с тремя бэкендами. Интерфейс одинаков:
      push(record)  / pop_all() / is_empty() / flush() / clear()
    """

    def __init__(self, path, max_size):
        backend = getattr(config, 'BUFFER_BACKEND', 'auto').lower()

        # Резолвим бэкенд
        if backend == "ram":
            self.backend = "ram"
        elif backend == "flash":
            self.backend = "flash"
        elif backend == "sd":
            self.backend = "sd" if _mount_sd() else "ram"
            if self.backend == "ram":
                _log("SD недоступна — fallback на RAM")
        else:  # auto
            if path.startswith(config.SD_MOUNT) and _mount_sd():
                self.backend = "sd"
            else:
                self.backend = "flash"

        self.max_size = max_size
        self.flush_min_ms = getattr(config, 'BUFFER_FLUSH_MIN_MS', 30_000)
        self.flush_max_recs = getattr(config, 'BUFFER_FLUSH_MAX_RECS', 16)
        self.ram_max = getattr(config, 'BUFFER_RAM_MAX_RECS', 256)

        if self.backend == "ram":
            self.path = None
            _log("backend=ram (max {} recs)".format(self.ram_max))
        elif self.backend == "flash":
            self.path = getattr(config, 'BUFFER_FALLBACK_PATH', "/data/offline.jsonl")
            _ensure_dir(self.path)
            _log("backend=flash → {}".format(self.path))
        else:  # sd
            self.path = path
            _ensure_dir(self.path)
            _log("backend=sd → {}".format(self.path))

        self._pending = []                  # RAM-очередь (для всех бэкендов)
        self._last_flush_ms = utime.ticks_ms()

    # --- внутренние ---

    def _size(self):
        if not self.path:
            return 0
        try:
            return os.stat(self.path)[6]
        except OSError:
            return 0

    def _flush(self, force=False):
        """Сбросить RAM-очередь на диск (для sd/flash). Для ram — no-op."""
        if self.backend == "ram":
            # для RAM просто следим за лимитом
            if len(self._pending) > self.ram_max:
                drop = len(self._pending) - self.ram_max
                _log("RAM overflow: drop {} oldest".format(drop))
                self._pending = self._pending[drop:]
            return

        if not self._pending:
            return
        elapsed = utime.ticks_diff(utime.ticks_ms(), self._last_flush_ms)
        if not force and elapsed < self.flush_min_ms and len(self._pending) < self.flush_max_recs:
            return

        try:
            cur_size = self._size()
            with open(self.path, "a") as f:
                for record in self._pending:
                    line = ujson.dumps(record) + "\n"
                    if len(line) >= self.max_size:
                        continue
                    if cur_size + len(line) > self.max_size:
                        f.close()
                        self._truncate()
                        cur_size = 0
                        f = open(self.path, "a")
                    f.write(line)
                    cur_size += len(line)
            _log("Flushed {} recs → {}".format(len(self._pending), self.backend))
        except Exception as e:
            _log("Flush failed: {}".format(e))
            return
        self._pending = []
        self._last_flush_ms = utime.ticks_ms()

    def _truncate(self):
        if not self.path:
            return
        try:
            with open(self.path, "w") as f:
                pass
        except OSError:
            pass

    # --- публичный API ---

    def push(self, record):
        self._pending.append(record)
        self._flush(force=False)

    def is_empty(self):
        if self.backend == "ram":
            return not self._pending
        return self._size() == 0 and not self._pending

    def pop_all(self):
        records = []
        if self.path:
            try:
                with open(self.path, "r") as f:
                    for line in f:
                        line = line.strip()
                        if not line:
                            continue
                        try:
                            records.append(ujson.loads(line))
                        except Exception:
                            pass
            except OSError:
                pass
        records.extend(self._pending)
        self._pending = []
        self._truncate()
        return records

    def flush(self):
        """Принудительный сброс RAM на диск (перед deepsleep)."""
        self._flush(force=True)

    def clear(self):
        self._pending = []
        if self.path:
            try:
                os.remove(self.path)
            except OSError:
                pass

    def shift_timestamps(self, offset, fields=("temperature_time", "noise_time")):
        """
        Прибавляет offset секунд к указанным timestamp-полям ВО ВСЕХ накопленных
        записях (и в RAM, и на диске). Используется когда выяснилось что RTC
        был сбит (NTP дошёл с большой задержкой / после потери питания).
        """
        if offset == 0:
            return
        # 1) Сдвигаем в RAM-очереди
        for rec in self._pending:
            for k in fields:
                v = rec.get(k, 0)
                if v and v > 0:
                    rec[k] = v + offset
        # 2) Сдвигаем на диске — читаем все, правим, перезаписываем
        if not self.path:
            return
        try:
            records = []
            with open(self.path, "r") as f:
                for line in f:
                    line = line.strip()
                    if not line:
                        continue
                    try:
                        rec = ujson.loads(line)
                        for k in fields:
                            v = rec.get(k, 0)
                            if v and v > 0:
                                rec[k] = v + offset
                        records.append(rec)
                    except Exception:
                        pass
            with open(self.path, "w") as f:
                for rec in records:
                    f.write(ujson.dumps(rec) + "\n")
            _log("Shifted {} records by {}s".format(len(records), offset))
        except Exception as e:
            _log("shift_timestamps failed: {}".format(e))
