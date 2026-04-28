# -*- coding: utf-8 -*-
"""
mqtt_test_server.py — Имитация серверной MQTT-логики на Python для отладки прошивки.

Что делает:
  1. Подписывается на /device/+/data и /device/+/status
  2. Печатает все входящие сообщения в консоль (с разбором JSON)
  3. На каждый /status отправляет /config (как делает реальный сервер
     в internal/domain/mqtt/handlers.go:222-236)

Запуск:
  1. Поднять Mosquitto:
       cd build && docker compose up -d mqtt_container
     Или одной командой без compose:
       docker run --rm -p 1883:1883 eclipse-mosquitto:2.0.15

  2. pip install paho-mqtt

  3. python tests/mqtt_test_server.py
"""

import argparse
import json
import sys
import time

try:
    import paho.mqtt.client as mqtt
except ImportError:
    print("Установи: pip install paho-mqtt", file=sys.stderr)
    sys.exit(1)


COLOR_RESET = "\033[0m"
COLOR_DATA   = "\033[36m"
COLOR_STATUS = "\033[33m"
COLOR_CONFIG = "\033[35m"
COLOR_ERR    = "\033[31m"
COLOR_OK     = "\033[32m"


def colored(text, color):
    return f"{color}{text}{COLOR_RESET}"


def parse_args():
    p = argparse.ArgumentParser()
    p.add_argument("--host", default="127.0.0.1", help="MQTT broker host")
    p.add_argument("--port", type=int, default=1883)
    p.add_argument("--sampling-noise", type=int, default=5,
                   help="Передаётся в config датчику")
    p.add_argument("--sampling-temp", type=int, default=5)
    p.add_argument("--frequency-status", type=int, default=30)
    p.add_argument("--restart", action="store_true", help="Послать restart_device=true в config")
    p.add_argument("--delete", action="store_true", help="Послать delete_device=true (отвязать датчик навсегда)")
    return p.parse_args()


def print_payload(prefix_color, label, device_id, payload):
    raw = payload.decode("utf-8", errors="replace")
    print(colored(f"\n[{label}]", prefix_color), f"device={device_id}")
    try:
        data = json.loads(raw)
        for k, v in data.items():
            print(f"   {k}: {v}")
    except json.JSONDecodeError:
        print(colored(f"   raw: {raw}", COLOR_ERR))


def make_config(args):
    return {
        "sampling_rate_noise":       args.sampling_noise,
        "sampling_rate_temperature": args.sampling_temp,
        "frequency_status":          args.frequency_status,
        "restart_device":            args.restart,
        "health_check":              False,
        "delete_device":             args.delete,
    }


def main():
    args = parse_args()
    seen_devices = set()

    def on_connect(client, userdata, flags, rc, properties=None):
        if rc == 0:
            print(colored(f"[OK] Connected to {args.host}:{args.port}", COLOR_OK))
            client.subscribe("/device/+/data", qos=1)
            client.subscribe("/device/+/status", qos=1)
            print("Подписан на /device/+/data и /device/+/status")
            print("Жду сообщения от датчика...\n")
        else:
            print(colored(f"[ERR] Connect failed rc={rc}", COLOR_ERR))

    def on_disconnect(client, userdata, rc, properties=None):
        print(colored(f"[--] Disconnected rc={rc}", COLOR_ERR))

    def on_message(client, userdata, msg):
        parts = msg.topic.split("/")
        if len(parts) != 4 or parts[1] != "device":
            print(colored(f"[?] Unexpected topic: {msg.topic}", COLOR_ERR))
            return

        device_id = parts[2]
        kind = parts[3]

        if kind == "data":
            print_payload(COLOR_DATA, "DATA", device_id, msg.payload)
        elif kind == "status":
            print_payload(COLOR_STATUS, "STATUS", device_id, msg.payload)
            # Имитируем поведение сервера: на любой status шлём config
            cfg = make_config(args)
            cfg_topic = f"/device/{device_id}/config"
            client.publish(cfg_topic, json.dumps(cfg), qos=1)
            print(colored(f"   → отправлен {cfg_topic}: {cfg}", COLOR_CONFIG))
            if device_id not in seen_devices:
                seen_devices.add(device_id)
                print(colored(f"   ✓ новый датчик зарегистрирован: {device_id}", COLOR_OK))
        else:
            print(colored(f"[?] Unknown kind: {kind}", COLOR_ERR))

    # paho-mqtt v2 API; для v1 убрать callback_api_version
    try:
        client = mqtt.Client(callback_api_version=mqtt.CallbackAPIVersion.VERSION2,
                             client_id="beeiot_test_server")
    except (AttributeError, TypeError):
        client = mqtt.Client(client_id="beeiot_test_server")

    client.on_connect = on_connect
    client.on_disconnect = on_disconnect
    client.on_message = on_message

    print(f"Подключаюсь к {args.host}:{args.port}...")
    try:
        client.connect(args.host, args.port, keepalive=60)
    except Exception as e:
        print(colored(f"[ERR] Не могу подключиться: {e}", COLOR_ERR))
        print("Проверь, что mosquitto запущен:")
        print("  docker run --rm -p 1883:1883 eclipse-mosquitto:2.0.15")
        sys.exit(1)

    try:
        client.loop_forever()
    except KeyboardInterrupt:
        print("\nЗавершение...")
        client.disconnect()


if __name__ == "__main__":
    main()
