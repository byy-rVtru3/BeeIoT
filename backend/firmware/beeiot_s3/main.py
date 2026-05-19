# -*- coding: utf-8 -*-
"""
main.py — Точка входа прошивки BeeIoT (ESP32-S3 + SIM7020C + WiFi backup).

Один цикл FSM:
  READ_SENSORS → CONNECT (модем → WiFi fallback) → SUBSCRIBE_CONFIG
    → PUBLISH_STATUS → WAIT_CONFIG → APPLY_CONFIG → PUBLISH_DATA (+ buffered)

Между циклами:
  - config.DEEP_SLEEP_ENABLED = True  → machine.deepsleep()
  - config.DEEP_SLEEP_ENABLED = False → utime.sleep_ms() (USB остаётся доступен)

Порядок важен: SUBSCRIBE_CONFIG ОБЯЗАТЕЛЬНО до первого PUBLISH_STATUS,
иначе сервер опубликует начальный конфиг (retained=false), а датчик его
не получит. См. handlers.go:174 на сервере.
"""

import gc
import machine
import utime

import config
from modem import SIM7020
from wifi_transport import WiFiMQTT
from temperature import TemperatureSensor
from noise import NoiseSensor
from ring_buffer import RingBuffer
import protocol


def _log(msg):
    if config.DEBUG:
        print("[MAIN]", msg)


def _apply_config(cfg):
    """
    Применяет полученную конфигурацию из топика /device/{id}/config.

    Поля с -1 → не менять. restart_device=True → перезагрузка.
    delete_device=True → стирание буфера + бесконечный deepsleep.
    """
    if not cfg:
        return
    _log("Apply config: {}".format(cfg))

    # Частоты от сервера игнорим если так в конфе. Подписываемся ради
    # restart_device/delete_device/health_check, всё.
    if getattr(config, 'APPLY_SERVER_INTERVALS', True):
        if cfg.get("sampling_rate_noise", -1) != -1:
            config.DEFAULT_SAMPLING_NOISE = cfg["sampling_rate_noise"]
            config.DEEP_SLEEP_MS = config.DEFAULT_SAMPLING_NOISE * 1_000

        if cfg.get("sampling_rate_temperature", -1) != -1:
            config.DEFAULT_SAMPLING_TEMP = cfg["sampling_rate_temperature"]

        if cfg.get("frequency_status", -1) != -1:
            config.DEFAULT_STATUS_FREQUENCY = cfg["frequency_status"]

    if cfg.get("delete_device", False):
        _log("delete_device=true → стирание буфера и бесконечный deepsleep")
        try:
            import os
            os.remove(config.BUFFER_FILE_PATH)
        except Exception:
            pass
        machine.deepsleep(0x7FFFFFFF)

    if cfg.get("restart_device", False):
        _log("restart_device=true → reset")
        machine.reset()


def _bring_up_modem():
    """Пытается поднять SIM7020 + MQTT. Возвращает (transport, ok)."""
    sim = SIM7020(
        uart_id=config.MODEM_UART_ID,
        tx=config.MODEM_TX_PIN,
        rx=config.MODEM_RX_PIN,
        baudrate=config.MODEM_BAUDRATE,
        pwrkey=config.MODEM_PWRKEY_PIN,
    )
    if not sim.power_on():
        return sim, False, "modem_power_on_failed"
    if not sim.attach_network():
        return sim, False, "network_register_failed"
    if not sim.mqtt_connect(
        broker=config.MQTT_BROKER,
        port=config.MQTT_PORT,
        client_id=config.DEVICE_ID,
        keepalive=config.MQTT_KEEPALIVE,
        user=config.MQTT_USER,
        password=config.MQTT_PASSWORD,
    ):
        return sim, False, "mqtt_connect_failed"
    return sim, True, None


def _bring_up_wifi():
    """Пытается поднять WiFi + MQTT. Возвращает (transport, ok)."""
    wifi = WiFiMQTT()
    if not wifi.power_on():
        return wifi, False, "wifi_connect_failed"
    if not wifi.mqtt_connect(
        broker=config.MQTT_BROKER,
        port=config.MQTT_PORT,
        client_id=config.DEVICE_ID,
        keepalive=config.MQTT_KEEPALIVE,
        user=config.MQTT_USER,
        password=config.MQTT_PASSWORD,
    ):
        return wifi, False, "wifi_mqtt_connect_failed"
    return wifi, True, None


_BUF = None   # глобальный буфер живёт между циклами (важно для RAM-режима!)


def _run_cycle():
    """Один полный цикл сбора и публикации."""
    global _BUF
    gc.collect()

    topic_data   = config.TOPIC_DATA.format(config.DEVICE_ID)
    topic_status = config.TOPIC_STATUS.format(config.DEVICE_ID)
    topic_cfg    = config.TOPIC_CONFIG.format(config.DEVICE_ID)

    if _BUF is None:
        _BUF = RingBuffer(config.BUFFER_FILE_PATH, config.BUFFER_MAX_SIZE_BYTES)
    buf = _BUF

    errors = []
    data_payload = None
    transport = None
    mqtt_ok = False

    try:
        # === READ_SENSORS ===
        _log("--- READ_SENSORS ---")
        # MicroPython epoch — 2000-01-01, добавляем разницу до 1970 чтобы
        # получить Unix-секунды (как ждёт сервер). До NTP-синка значения
        # будут смещены, но хотя бы в правильном диапазоне.
        ts = utime.time() + 946_684_800

        # Каждый датчик инициализируем в отдельном try/except — если фейлится
        # сама инициализация (1-Wire не нашёл устройство, I2S не открылся),
        # цикл должен ехать дальше, а тег ошибки лететь в status.errors[],
        # иначе сервер вообще не узнает, что у датчика что-то отвалилось.
        temperature = -1.0
        try:
            temp_sensor = TemperatureSensor(config.DS18B20_PIN)
            temperature = temp_sensor.read()
            del temp_sensor
            if temperature == -1.0:
                errors.append("temperature_read_error")
        except Exception as e:
            _log("temperature sensor init/read failed: {}".format(e))
            errors.append("temperature_init_error")

        noise = -1.0
        try:
            noise_sensor = NoiseSensor()
            try:
                noise = noise_sensor.read()
                if noise == -1.0:
                    errors.append("noise_read_error")
            finally:
                noise_sensor.deinit()
                del noise_sensor
        except Exception as e:
            _log("noise sensor init/read failed: {}".format(e))
            errors.append("noise_init_error")
        gc.collect()

        _log("T={} °C, N={} dB".format(temperature, noise))
        data_payload = protocol.make_data_payload(temperature, noise, ts)

        # === CONNECT_NETWORK (модем → WiFi fallback) ===
        if getattr(config, 'MODEM_ENABLED', True):
            _log("--- CONNECT_NETWORK (modem) ---")
            transport, mqtt_ok, err = _bring_up_modem()
            if not mqtt_ok:
                errors.append(err)
                _log("modem failed: {} → trying WiFi".format(err))
                try:
                    transport.power_off()
                except Exception:
                    pass
                transport = None
        if not mqtt_ok and getattr(config, 'WIFI_ENABLED', False):
            _log("--- CONNECT_NETWORK (wifi) ---")
            transport, mqtt_ok, err = _bring_up_wifi()
            if not mqtt_ok:
                errors.append(err)

        if not mqtt_ok:
            _log("All transports failed → buffer push")
            buf.push(data_payload)
            buf.flush()
            return

        # === Network time + RSSI ===
        net_ts = transport.network_time()
        if net_ts:
            # Если до этого RTC был сбит (потеря питания, NTP не дошёл),
            # все накопленные записи имеют битый ts. Сдвигаем их на дельту
            # между реальным временем и тем что было в utime.
            local_now = utime.time() + 946_684_800
            offset = net_ts - local_now
            if offset > 60 or offset < -60:
                _log("RTC was off by {}s, shifting buffered records".format(offset))
                buf.shift_timestamps(offset)
                # текущий payload тоже исправляем
                data_payload["temperature_time"] += offset
                data_payload["noise_time"] += offset
            ts = net_ts
            data_payload["temperature_time"] = ts
            data_payload["noise_time"] = ts
        signal = transport.signal_strength()

        # === SUBSCRIBE_CONFIG (ДО первого status!) ===
        _log("--- SUBSCRIBE_CONFIG ---")
        transport.mqtt_subscribe(topic_cfg)

        # === PUBLISH_STATUS ===
        _log("--- PUBLISH_STATUS ---")
        status_payload = protocol.make_status_payload(
            battery=config.BATTERY_LEVEL_DEFAULT,
            signal=signal,
            ts=ts,
            errors=errors,
        )
        transport.mqtt_publish(topic_status, protocol.dumps(status_payload))

        # === WAIT_CONFIG ===
        _log("--- WAIT_CONFIG ({} ms) ---".format(config.MQTT_CONFIG_WAIT_MS))
        msg = transport.mqtt_wait_msg(config.MQTT_CONFIG_WAIT_MS)
        if msg:
            _topic, payload = msg
            _apply_config(protocol.parse_config(payload))
        else:
            _log("No config received (timeout)")

        # === PUBLISH_DATA (буфер + текущая запись) ===
        _log("--- PUBLISH_DATA ---")
        if not buf.is_empty():
            backlog = buf.pop_all()
            _log("flushing {} buffered records".format(len(backlog)))
            for record in backlog:
                transport.mqtt_publish(topic_data, protocol.dumps(record))
            gc.collect()

        transport.mqtt_publish(topic_data, protocol.dumps(data_payload))

    except Exception as e:
        _log("FATAL in cycle: {}".format(e))
        if data_payload is not None:
            try:
                buf.push(data_payload)
                buf.flush()
            except Exception:
                pass

    finally:
        if transport is not None:
            if mqtt_ok:
                try:
                    transport.mqtt_disconnect()
                except Exception:
                    pass
            try:
                transport.power_off()
            except Exception:
                pass
        # перед возможным deepsleep гарантированно сбрасываем RAM-кэш буфера
        try:
            buf.flush()
        except Exception:
            pass


def run():
    """
    Главный цикл. Если DEEP_SLEEP_ENABLED=False — крутится бесконечно
    с обычным sleep между итерациями (USB остаётся доступен).
    Если True — после первой итерации уходит в deepsleep.
    """
    if config.DEEP_SLEEP_ENABLED:
        _log("=== BeeIoT wakeup (deepsleep mode) ===")
        _run_cycle()
        _log("--- DEEPSLEEP {} ms ---".format(config.DEEP_SLEEP_MS))
        machine.deepsleep(config.DEEP_SLEEP_MS)
    else:
        _log("=== BeeIoT start (always-on mode, deepsleep DISABLED) ===")
        while True:
            _run_cycle()
            _log("--- SLEEP {} ms (USB stays alive) ---".format(config.DEEP_SLEEP_MS))
            utime.sleep_ms(config.DEEP_SLEEP_MS)


run()
