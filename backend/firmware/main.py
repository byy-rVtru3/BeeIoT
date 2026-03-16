# -*- coding: utf-8 -*-
"""
main.py — Точка входа прошивки BeeIoT. Конечный автомат (FSM).

Жизненный цикл:
  INIT → READ_SENSORS → CONNECT_NETWORK → PUBLISH → LISTEN_CONFIG → SLEEP

КРИТИЧЕСКИ ВАЖНО:
  Блок finally гарантирует выключение SIM800L и вызов machine.deepsleep()
  при ЛЮБОМ сценарии завершения (включая необработанные исключения).
"""

import machine
import utime
import ujson
import gc

import config
from sensors.temperature import TemperatureSensor
from sensors.noise import NoiseSensor
from network.sim800l import SIM800L
from storage.ring_buffer import RingBuffer


def _log(msg):
    if config.DEBUG:
        print("[MAIN]", msg)


def _get_signal_strength(sim: SIM800L) -> int:
    """
    Запрашивает уровень сигнала через AT+CSQ.

    :return: Качество сигнала 0–100% или -1 если недоступно
    """
    try:
        resp = sim._send_at("AT+CSQ", "+CSQ:", config.SIM800L_AT_DEFAULT_TIMEOUT_MS)
        if resp and "+CSQ:" in resp:
            # Ответ: +CSQ: <rssi>,<ber>
            # rssi: 0–31 (31 = максимум), 99 = неизвестно
            part = resp.split("+CSQ:")[1].split(",")[0].strip()
            rssi = int(part)
            if rssi == 99 or rssi < 0:
                return -1
            # Нормируем 0–31 → 0–100%
            return int(rssi * 100 / 31)
    except Exception:
        pass
    return -1


def _apply_config(cfg: dict) -> None:
    """
    Применяет полученную конфигурацию из топика /device/{id}/config.

    Поля с -1 игнорируются (не меняют текущую настройку).
    restart_device=true → перезагрузка устройства.
    delete_device=true → стирание буфера и уход в деepsleep навсегда.
    """
    try:
        _log("Применение конфигурации: {}".format(cfg))

        if cfg.get("sampling_rate_noise", -1) != -1:
            config.DEFAULT_SAMPLING_NOISE = cfg["sampling_rate_noise"]
            _log("sampling_rate_noise → {}".format(config.DEFAULT_SAMPLING_NOISE))

        if cfg.get("sampling_rate_temperature", -1) != -1:
            config.DEFAULT_SAMPLING_TEMP = cfg["sampling_rate_temperature"]
            _log("sampling_rate_temperature → {}".format(config.DEFAULT_SAMPLING_TEMP))

        if cfg.get("frequency_status", -1) != -1:
            config.DEFAULT_STATUS_FREQUENCY = cfg["frequency_status"]
            _log("frequency_status → {}".format(config.DEFAULT_STATUS_FREQUENCY))

        if cfg.get("health_check", False):
            _log("health_check запрошен — выполняется в следующем цикле")

        if cfg.get("delete_device", False):
            _log("delete_device=true — стирание буфера")
            try:
                import os
                os.remove(config.BUFFER_FILE_PATH)
            except Exception:
                pass
            # Уходим в бесконечный deep sleep (фактически отключение)
            machine.deepsleep(0x7FFFFFFF)

        if cfg.get("restart_device", False):
            _log("restart_device=true — перезагрузка")
            machine.reset()

    except Exception as e:
        _log("ОШИБКА _apply_config: {}".format(e))


def run() -> None:
    """
    Главная функция: один полный жизненный цикл устройства.

    Вызывается один раз после пробуждения из Deep Sleep.
    Блок finally всегда выполняет power_off() + deepsleep().
    """
    _log("=== BeeIoT пробуждение ===")
    gc.collect()

    sim = SIM800L(
        uart_id=config.SIM800L_UART_ID,
        tx=config.SIM800L_TX_PIN,
        rx=config.SIM800L_RX_PIN,
        rst=config.SIM800L_RST_PIN,
        baudrate=config.SIM800L_BAUDRATE,
    )
    buf = RingBuffer(config.BUFFER_FILE_PATH, config.BUFFER_MAX_SIZE_BYTES)

    # MQTT топики для этого датчика
    topic_data   = config.TOPIC_DATA.format(config.SENSOR_ID)
    topic_status = config.TOPIC_STATUS.format(config.SENSOR_ID)
    topic_cfg    = config.TOPIC_CONFIG.format(config.SENSOR_ID)

    errors = []
    data_payload = None    # Инициализация до try-блока: защита от NameError в except

    try:
        # ============================================================
        # STATE: READ_SENSORS
        # ============================================================
        _log("--- READ_SENSORS ---")
        gc.collect()

        now = utime.time()

        temp_sensor  = TemperatureSensor(config.DS18B20_PIN)
        noise_sensor = NoiseSensor(config.KY038_ADC_PIN)

        temperature = temp_sensor.read()
        noise       = noise_sensor.read()

        if temperature == -1.0:
            errors.append("temperature_read_error")
        if noise == -1.0:
            errors.append("noise_read_error")

        _log("Температура: {}, Шум: {} dB".format(temperature, noise))

        # Полезные данные — строго по формату docs/sensor_communication.md
        data_payload = {
            "temperature":      temperature,
            "temperature_time": now,
            "noise":            noise,
            "noise_time":       now,
        }

        # Статус — строго по формату docs/sensor_communication.md
        status_payload = {
            "battery_level":   -1,    # ADC батареи не подключён
            "signal_strength": -1,    # Обновится после power_on
            "timestamp":       now,
            "errors":          errors,
        }

        # Освобождаем RAM от объектов датчиков
        del temp_sensor, noise_sensor
        gc.collect()

        # ============================================================
        # STATE: CONNECT_NETWORK
        # ============================================================
        _log("--- CONNECT_NETWORK ---")

        if not sim.power_on():
            _log("Сеть недоступна — сохраняем в буфер")
            buf.push(data_payload)
            return   # → finally: power_off + deepsleep

        # Обновляем уровень сигнала после подключения к сети
        signal = _get_signal_strength(sim)
        status_payload["signal_strength"] = signal

        if not sim.connect_gprs():
            _log("GPRS недоступен — сохраняем в буфер")
            buf.push(data_payload)
            return   # → finally

        if not sim.mqtt_connect(
            broker=config.MQTT_BROKER,
            port=config.MQTT_PORT,
            client_id=config.SENSOR_ID,
            user=config.MQTT_USER,
            password=config.MQTT_PASSWORD,
            keepalive=config.MQTT_KEEPALIVE,
        ):
            _log("MQTT соединение не удалось — сохраняем в буфер")
            buf.push(data_payload)
            return   # → finally

        # ============================================================
        # STATE: PUBLISH
        # ============================================================
        _log("--- PUBLISH ---")

        # Сначала — накопленные офлайн-данные (актуальность важнее истории)
        if not buf.is_empty():
            _log("Отправка буферизованных данных...")
            offline_records = buf.pop_all()
            for record in offline_records:
                success = sim.mqtt_publish(topic_data, ujson.dumps(record))
                if not success:
                    _log("WARN: не удалось отправить запись из буфера")
            del offline_records
            gc.collect()

        # Текущие данные
        sim.mqtt_publish(topic_data,   ujson.dumps(data_payload))
        sim.mqtt_publish(topic_status, ujson.dumps(status_payload))
        _log("Данные отправлены")

        # ============================================================
        # STATE: LISTEN_CONFIG
        # ============================================================
        _log("--- LISTEN_CONFIG (ожидание {} мс) ---".format(
            config.MQTT_CONFIG_WAIT_MS))

        sim.mqtt_subscribe(topic_cfg)
        cfg_msg = sim.mqtt_wait_msg(config.MQTT_CONFIG_WAIT_MS)

        if cfg_msg is not None:
            _log("Получена конфигурация: {}".format(cfg_msg))
            sim.mqtt_disconnect()   # Отключить до apply_config (может вызвать reset)
            _apply_config(cfg_msg)
        else:
            _log("Конфигурация не получена (таймаут)")
            sim.mqtt_disconnect()

    except Exception as e:
        # Глобальный перехватчик — любая необработанная ошибка
        _log("КРИТИЧЕСКАЯ ОШИБКА: {}".format(e))
        # Пытаемся сохранить данные если они были успешно собраны
        try:
            if data_payload is not None:
                buf.push(data_payload)
        except Exception:
            pass

    finally:
        # ============================================================
        # STATE: SLEEP — всегда выполняется последним
        # ============================================================
        _log("--- SLEEP ---")
        try:
            sim.power_off()
        except Exception:
            pass
        _log("Уходим в Deep Sleep на {} мс".format(config.DEEP_SLEEP_DURATION_MS))
        machine.deepsleep(config.DEEP_SLEEP_DURATION_MS)


# Точка входа
run()
