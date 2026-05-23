# -*- coding: utf-8 -*-
"""
protocol.py — Формирование и парсинг сообщений по протоколу сервера BeeIoT.

Соответствует Go-структурам сервера:
  internal/domain/models/mqttTypes/types.go
"""

import ujson


def make_data_payload(temperature, noise, ts):
    """
    /device/{id}/data — DeviceData

    -1 = "нет данных" (сервер пропустит запись соответствующего поля).
    Вес шлём -1/0 — у этого устройства нет тензодатчика.
    """
    return {
        "temperature":      temperature if temperature is not None else -1,
        "temperature_time": ts,
        "noise":            noise if noise is not None else -1,
        "noise_time":       ts,
        "weight":           -1,
        "weight_time":      0,
    }


def make_status_payload(battery, signal, ts, errors):
    """
    /device/{id}/status — DeviceStatus
    """
    return {
        "battery_level":   battery if battery is not None else -1,
        "signal_strength": signal if signal is not None else -1,
        "timestamp":       ts,
        "errors":          errors or [],
    }


def parse_config(raw):
    """
    /device/{id}/config — DeviceConfig
    На вход — байты или строка JSON.
    """
    if isinstance(raw, (bytes, bytearray)):
        raw = raw.decode("utf-8", "ignore")
    try:
        return ujson.loads(raw)
    except Exception:
        return None


def dumps(obj):
    return ujson.dumps(obj)
