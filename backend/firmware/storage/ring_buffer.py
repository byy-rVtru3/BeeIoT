# -*- coding: utf-8 -*-
"""
storage/ring_buffer.py — Кольцевой буфер для хранения JSON-данных во flash.

Формат файла: JSONL (одна запись = одна строка JSON).
При отсутствии сети данные сохраняются здесь и отправляются при восстановлении.

Гарантии:
  - Файл не превысит BUFFER_MAX_SIZE_BYTES (при переполнении удаляет старые записи)
  - Все методы безопасны: не выбрасывают исключения наружу
  - Оптимизирован для минимума операций записи во flash
"""

import os
import ujson

import config


def _log(msg):
    if config.DEBUG:
        print("[BUFFER]", msg)


class RingBuffer:
    """
    Кольцевой буфер на основе JSONL-файла во flash-памяти ESP32.

    Пример использования:
        buf = RingBuffer(config.BUFFER_FILE_PATH, config.BUFFER_MAX_SIZE_BYTES)
        buf.push({"temperature": 23.5, ...})
        records = buf.pop_all()   # list of dicts
    """

    def __init__(self, filepath: str, max_size_bytes: int):
        """
        :param filepath:      Путь к файлу буфера (из config.BUFFER_FILE_PATH)
        :param max_size_bytes: Максимальный размер файла в байтах
        """
        self._path = filepath
        self._max = max_size_bytes
        self._ensure_dir()

    # ------------------------------------------------------------------
    # Публичный API
    # ------------------------------------------------------------------

    def push(self, record: dict) -> None:
        """
        Добавляет запись в буфер.

        Если после добавления файл превышает лимит, удаляет самую старую запись.

        :param record: Словарь с данными для сохранения
        """
        try:
            line = ujson.dumps(record) + "\n"
            with open(self._path, "a") as f:
                f.write(line)
            _log("Запись добавлена ({} байт)".format(len(line)))
            # Проверяем размер и обрезаем если нужно
            self._trim_if_needed()
        except Exception as e:
            _log("ОШИБКА push: {}".format(e))

    def pop_all(self) -> list:
        """
        Читает и удаляет все записи из буфера.

        Повреждённые строки автоматически пропускаются (не прерывают работу).

        :return: Список dict (может быть пустым)
        """
        records = []
        if not self._file_exists():
            return records
        try:
            with open(self._path, "r") as f:
                for line in f:
                    line = line.strip()
                    if not line:
                        continue
                    try:
                        records.append(ujson.loads(line))
                    except Exception:
                        _log("Пропуск повреждённой строки: {}".format(line[:40]))
            # Удаляем файл после успешного чтения
            os.remove(self._path)
            _log("Буфер прочитан: {} записей, файл удалён".format(len(records)))
        except Exception as e:
            _log("ОШИБКА pop_all: {}".format(e))
        return records

    def is_empty(self) -> bool:
        """
        Проверяет, пуст ли буфер.

        :return: True если файл не существует или имеет нулевой размер
        """
        try:
            return not self._file_exists() or os.stat(self._path)[6] == 0
        except Exception:
            return True

    # ------------------------------------------------------------------
    # Внутренние методы
    # ------------------------------------------------------------------

    def _ensure_dir(self) -> None:
        """Создаёт директорию для буфера если она не существует."""
        try:
            dir_path = self._path.rsplit("/", 1)[0]
            if dir_path and dir_path != "/":
                try:
                    os.stat(dir_path)
                except OSError:
                    os.mkdir(dir_path)
                    _log("Создана директория: {}".format(dir_path))
        except Exception as e:
            _log("ОШИБКА _ensure_dir: {}".format(e))

    def _file_exists(self) -> bool:
        """Проверяет наличие файла буфера."""
        try:
            os.stat(self._path)
            return True
        except OSError:
            return False

    def _file_size(self) -> int:
        """Возвращает текущий размер файла в байтах, 0 при ошибке."""
        try:
            return os.stat(self._path)[6]
        except Exception:
            return 0

    def _trim_if_needed(self) -> None:
        """
        Уменьшает буфер если превышен лимит.

        Удаляет по одной самой старой записи за раз до тех пор,
        пока размер файла не войдёт в лимит. Это гарантирует,
        что каждая запись в буфере соответствует одному циклу данных.
        """
        while self._file_size() > self._max:
            removed = self._remove_oldest()
            if not removed:
                # Нечего удалять — буфер пуст или повреждён
                break

    def _remove_oldest(self) -> bool:
        """
        Удаляет одну самую старую(первую) запись из файла.

        :return: True если запись удалена, False если файл пуст или ошибка
        """
        try:
            with open(self._path, "r") as f:
                lines = f.readlines()

            if not lines:
                return False

            # Отбрасываем первую (самую старую) строку
            remaining = lines[1:]

            if remaining:
                with open(self._path, "w") as f:
                    for line in remaining:
                        f.write(line)
            else:
                # Файл стал пустым — удаляем его
                os.remove(self._path)

            _log("Удалена старая запись, осталось: {}".format(len(remaining)))
            return True
        except Exception as e:
            _log("ОШИБКА _remove_oldest: {}".format(e))
            return False
