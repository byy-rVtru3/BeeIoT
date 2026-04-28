# -*- coding: utf-8 -*-
"""
ring_buffer.py — JSON-Lines файл-буфер для оффлайн-хранения данных.

При отсутствии сети прошивка пишет несостоявшиеся публикации в файл,
при следующем успешном коннекте — отправляет всё накопленное и чистит буфер.
Размер ограничен BUFFER_MAX_SIZE_BYTES — старые записи отбрасываются.
"""

import os
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


class RingBuffer:
    def __init__(self, path, max_size):
        self.path = path
        self.max_size = max_size
        _ensure_dir(path)

    def _size(self):
        try:
            return os.stat(self.path)[6]
        except OSError:
            return 0

    def push(self, record):
        line = ujson.dumps(record) + "\n"
        # Если переполнение — обрезаем, оставляем только текущую запись
        if len(line) >= self.max_size:
            _log("Record too big, dropped")
            return
        if self._size() + len(line) > self.max_size:
            _log("Buffer full, truncating")
            self.clear()
        try:
            with open(self.path, "a") as f:
                f.write(line)
        except Exception as e:
            _log("Push failed: {}".format(e))

    def is_empty(self):
        return self._size() == 0

    def pop_all(self):
        """
        Возвращает все записи и очищает буфер. На ошибке парсинга
        повреждённые строки пропускаются.
        """
        records = []
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
            self.clear()
        except OSError:
            pass
        return records

    def clear(self):
        try:
            os.remove(self.path)
        except OSError:
            pass
