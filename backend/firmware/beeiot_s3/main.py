# -*- coding: utf-8 -*-
"""
main.py — Точка входа прошивки BeeIoT (ESP32-S3 + SIM7020C).

Один цикл FSM:
  READ_SENSORS → CONNECT_NETWORK → SUBSCRIBE_CONFIG → PUBLISH_STATUS
    → WAIT_CONFIG → APPLY_CONFIG → PUBLISH_DATA (+ buffered)

Между циклами:
  - config.DEEP_SLEEP_ENABLED = True  → machine.deepsleep() (низкое потребление)
  - config.DEEP_SLEEP_ENABLED = False → utime.sleep_ms() (USB остаётся доступен,
                                                          удобно для отладки)

Порядок важен: SUBSCRIBE_CONFIG ОБЯЗАТЕЛЬНО до первого PUBLISH_STATUS,
иначе сервер опубликует начальный конфиг (retained=false), а датчик его
не получит. См. handlers.go:174 на сервере.
"""

import gc
import machine
import utime

import config
from modem import SIM7020
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


def _run_cycle():
    """Один полный цикл сбора и публикации."""
    gc.collect()

    topic_data   = config.TOPIC_DATA.format(config.DEVICE_ID)
    topic_status = config.TOPIC_STATUS.format(config.DEVICE_ID)
    topic_cfg    = config.TOPIC_CONFIG.format(config.DEVICE_ID)

    sim = SIM7020(
        uart_id=config.MODEM_UART_ID,
        tx=config.MODEM_TX_PIN,
        rx=config.MODEM_RX_PIN,
        baudrate=config.MODEM_BAUDRATE,
        pwrkey=config.MODEM_PWRKEY_PIN,
    )
    buf = RingBuffer(config.BUFFER_FILE_PATH, config.BUFFER_MAX_SIZE_BYTES)

    errors = []
    data_payload = None
    mqtt_ok = False

    try:
        # === READ_SENSORS ===
        _log("--- READ_SENSORS ---")
        ts = utime.time()

        temp_sensor = TemperatureSensor(config.DS18B20_PIN)
        temperature = temp_sensor.read()
        del temp_sensor

        noise_sensor = NoiseSensor()
        noise = noise_sensor.read()
        noise_sensor.deinit()
        del noise_sensor
        gc.collect()

        if temperature == -1.0:
            errors.append("temperature_read_error")
        if noise == -1.0:
            errors.append("noise_read_error")

        _log("T={} °C, N={} dB".format(temperature, noise))
        data_payload = protocol.make_data_payload(temperature, noise, ts)

        # === CONNECT_NETWORK ===
        _log("--- CONNECT_NETWORK ---")
        if not sim.power_on():
            errors.append("modem_power_on_failed")
            buf.push(data_payload)
            return

        if not sim.attach_network():
            errors.append("network_register_failed")
            buf.push(data_payload)
            return

        net_ts = sim.network_time()
        if net_ts:
            ts = net_ts
            data_payload["temperature_time"] = ts
            data_payload["noise_time"] = ts

        signal = sim.signal_strength()

        if not sim.mqtt_connect(
            broker=config.MQTT_BROKER,
            port=config.MQTT_PORT,
            client_id=config.DEVICE_ID,
            keepalive=config.MQTT_KEEPALIVE,
            user=config.MQTT_USER,
            password=config.MQTT_PASSWORD,
        ):
            errors.append("mqtt_connect_failed")
            buf.push(data_payload)
            return
        mqtt_ok = True

        # === SUBSCRIBE_CONFIG (ДО первого status!) ===
        _log("--- SUBSCRIBE_CONFIG ---")
        sim.mqtt_subscribe(topic_cfg)

        # === PUBLISH_STATUS ===
        _log("--- PUBLISH_STATUS ---")
        status_payload = protocol.make_status_payload(
            battery=config.BATTERY_LEVEL_DEFAULT,
            signal=signal,
            ts=ts,
            errors=errors,
        )
        sim.mqtt_publish(topic_status, protocol.dumps(status_payload))

        # === WAIT_CONFIG ===
        _log("--- WAIT_CONFIG ({} ms) ---".format(config.MQTT_CONFIG_WAIT_MS))
        msg = sim.mqtt_wait_msg(config.MQTT_CONFIG_WAIT_MS)
        if msg:
            _topic, payload = msg
            _apply_config(protocol.parse_config(payload))
        else:
            _log("No config received (timeout)")

        # === PUBLISH_DATA (буфер + текущая запись) ===
        _log("--- PUBLISH_DATA ---")
        if not buf.is_empty():
            for record in buf.pop_all():
                sim.mqtt_publish(topic_data, protocol.dumps(record))
            gc.collect()

        sim.mqtt_publish(topic_data, protocol.dumps(data_payload))

    except Exception as e:
        _log("FATAL in cycle: {}".format(e))
        if data_payload is not None:
            try:
                buf.push(data_payload)
            except Exception:
                pass

    finally:
        if mqtt_ok:
            try:
                sim.mqtt_disconnect()
            except Exception:
                pass
        try:
            sim.power_off()
        except Exception:
            pass


def run():
    """
    Главный цикл. Если DEEP_SLEEP_ENABLED=False — крутится бесконечно
    с обычным sleep между итерациями (USB остаётся доступен).
    Если True — после первой итерации уходит в deepsleep (просыпается через DEEP_SLEEP_MS,
    main.py запускается заново через boot.py).
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
