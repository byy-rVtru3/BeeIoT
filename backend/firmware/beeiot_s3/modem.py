# -*- coding: utf-8 -*-
"""
modem.py — Драйвер SIM7020C (NB-IoT) с встроенным MQTT через AT-команды.

В отличие от SIM800L, у SIM7020 MQTT-стек встроен в прошивку модема (AT+CMQ*).
Поэтому отдельный umqtt-клиент не нужен — публикация идёт одной AT-командой.

Жизненный цикл:
  power_on() → attach_network() → mqtt_connect() → mqtt_subscribe()
  → mqtt_publish() / mqtt_wait_msg() → mqtt_disconnect() → power_off()
"""

import utime
from machine import UART, Pin
import config


def _log(msg):
    if config.DEBUG:
        print("[MODEM]", msg)


class SIM7020:
    def __init__(self, uart_id, tx, rx, baudrate=115200, pwrkey=-1):
        self.uart = UART(uart_id, baudrate=baudrate, tx=tx, rx=rx, timeout=200, timeout_char=20)
        self.pwrkey = Pin(pwrkey, Pin.OUT) if pwrkey >= 0 else None
        self._mqtt_id = -1          # индекс MQTT-сессии, выданный AT+CMQNEW
        self._rx_buf = b""          # хвост от прошлого чтения (для URC)

    # ===================================================================
    # Низкоуровневая работа с UART
    # ===================================================================

    def _drain(self):
        """Сбросить всё, что висит во входном буфере."""
        utime.sleep_ms(20)
        while self.uart.any():
            self.uart.read()
            utime.sleep_ms(10)
        self._rx_buf = b""

    def _read_until(self, deadline_ms, terminator=None):
        """
        Читает из UART до deadline_ms или до встречи terminator.
        Возвращает накопленные байты (включая хвост от предыдущего чтения).
        """
        out = self._rx_buf
        self._rx_buf = b""
        while utime.ticks_diff(deadline_ms, utime.ticks_ms()) > 0:
            if self.uart.any():
                chunk = self.uart.read()
                if chunk:
                    out += chunk
                    if terminator and terminator in out:
                        return out
            else:
                utime.sleep_ms(20)
        return out

    def _send_at(self, cmd, expect="OK", timeout_ms=None):
        """
        Отправляет AT-команду, ждёт ответ.

        :return: декодированную строку ответа если содержит expect, иначе None
        """
        if timeout_ms is None:
            timeout_ms = config.MODEM_AT_TIMEOUT_MS

        self._drain()
        if config.DEBUG:
            _log(">> {}".format(cmd))
        self.uart.write((cmd + "\r\n").encode())

        deadline = utime.ticks_add(utime.ticks_ms(), timeout_ms)
        raw = b""
        while utime.ticks_diff(deadline, utime.ticks_ms()) > 0:
            if self.uart.any():
                raw += self.uart.read()
                try:
                    text = raw.decode("utf-8", "ignore")
                except Exception:
                    text = ""
                if expect in text:
                    if config.DEBUG:
                        _log("<< {}".format(text.strip()))
                    return text
                if "ERROR" in text:
                    if config.DEBUG:
                        _log("<< ERROR: {}".format(text.strip()))
                    return None
            else:
                utime.sleep_ms(20)
        if config.DEBUG:
            _log("<< TIMEOUT (got {} bytes)".format(len(raw)))
        return None

    # ===================================================================
    # Включение / выключение
    # ===================================================================

    def power_on(self):
        """Включает модем (если есть PWRKEY) и ждёт готовность AT."""
        if self.pwrkey is not None:
            _log("Pulse PWRKEY")
            self.pwrkey.value(0)
            utime.sleep_ms(700)
            self.pwrkey.value(1)
            utime.sleep_ms(2000)

        # Ждём готовность с ретраями
        deadline = utime.ticks_add(utime.ticks_ms(), config.MODEM_BOOT_TIMEOUT_MS)
        while utime.ticks_diff(deadline, utime.ticks_ms()) > 0:
            if self._send_at("AT", "OK", 1000):
                self._send_at("ATE0", "OK")              # эхо выключаем
                self._send_at("AT+CMEE=2", "OK")          # вербальные ошибки
                _log("Modem ready")
                return True
            utime.sleep_ms(500)
        _log("Modem AT failed")
        return False

    def power_off(self):
        """Корректное отключение."""
        try:
            self._send_at("AT+CPOWD=1", "OK", 2000)
        except Exception:
            pass

    # ===================================================================
    # Регистрация в сети NB-IoT
    # ===================================================================

    def attach_network(self):
        """
        Регистрация в сети и поднятие PDP-контекста.

        Может занимать до минуты при холодном старте.
        """
        # Полная функциональность RF
        if not self._send_at("AT+CFUN=1", "OK", 10_000):
            return False

        # APN
        cmd = 'AT+CGDCONT=1,"IP","{}"'.format(config.APN)
        if not self._send_at(cmd, "OK"):
            _log("CGDCONT failed")
            return False

        # Attach to PS
        if not self._send_at("AT+CGATT=1", "OK", 30_000):
            _log("CGATT failed")
            return False

        # Ждём регистрации в сети (CEREG для NB-IoT)
        deadline = utime.ticks_add(utime.ticks_ms(), config.MODEM_REGISTER_TIMEOUT_MS)
        while utime.ticks_diff(deadline, utime.ticks_ms()) > 0:
            resp = self._send_at("AT+CEREG?", "+CEREG:", 5000)
            if resp:
                # Формат: +CEREG: <n>,<stat>[,...]
                # stat: 1 = registered home, 5 = registered roaming
                try:
                    stat = int(resp.split("+CEREG:")[1].split(",")[1].strip().split("\r")[0])
                    if stat in (1, 5):
                        _log("Registered (CEREG stat={})".format(stat))
                        return True
                except Exception:
                    pass
            utime.sleep_ms(2000)

        _log("Network registration timeout")
        return False

    def signal_strength(self):
        """
        Возвращает уровень сигнала 0–100% (-1 если недоступно).
        """
        resp = self._send_at("AT+CSQ", "+CSQ:")
        if not resp:
            return -1
        try:
            rssi = int(resp.split("+CSQ:")[1].split(",")[0].strip())
            if rssi == 99 or rssi < 0:
                return -1
            return int(rssi * 100 / 31)
        except Exception:
            return -1

    def network_time(self):
        """
        Возвращает UNIX-секунды по сети, либо None.
        Формат CCLK: +CCLK: "yy/MM/dd,hh:mm:ss±zz"
        """
        resp = self._send_at('AT+CCLK?', "+CCLK:")
        if not resp:
            return None
        try:
            s = resp.split('"')[1]
            date, rest = s.split(",")
            yy, mm, dd = [int(x) for x in date.split("/")]
            time_part = rest.split("+")[0].split("-")[0]
            hh, mi, ss = [int(x) for x in time_part.split(":")]
            # mktime ожидает 8-tuple
            return utime.mktime((2000 + yy, mm, dd, hh, mi, ss, 0, 0))
        except Exception:
            return None

    # ===================================================================
    # MQTT через встроенный стек SIM7020
    # ===================================================================

    def mqtt_connect(self, broker, port, client_id, keepalive=60, user="", password=""):
        """
        Создаёт MQTT-сессию и подключается.

        :return: True при успехе.
        """
        # AT+CMQNEW=<host>,<port>,<command_timeout>,<buffer_size>
        cmd = 'AT+CMQNEW="{}","{}",{},{}'.format(broker, port,
                                                  config.MQTT_CONNECT_TIMEOUT_MS,
                                                  1024)
        resp = self._send_at(cmd, "+CMQNEW:", config.MQTT_CONNECT_TIMEOUT_MS + 5000)
        if not resp:
            return False
        try:
            self._mqtt_id = int(resp.split("+CMQNEW:")[1].strip().split("\r")[0])
        except Exception:
            _log("Failed to parse CMQNEW id")
            return False

        # AT+CMQCON=<id>,<version>,<client_id>,<keepalive>,<cleansession>,<willflag>
        # version=3 (MQTT 3.1), cleansession=1, willflag=0
        if user:
            cmd = 'AT+CMQCON={},3,"{}",{},1,0,"{}","{}"'.format(
                self._mqtt_id, client_id, keepalive, user, password)
        else:
            cmd = 'AT+CMQCON={},3,"{}",{},1,0'.format(
                self._mqtt_id, client_id, keepalive)
        if not self._send_at(cmd, "OK", config.MQTT_CONNECT_TIMEOUT_MS):
            _log("CMQCON failed")
            return False
        _log("MQTT connected (id={})".format(self._mqtt_id))
        return True

    def mqtt_subscribe(self, topic, qos=1):
        cmd = 'AT+CMQSUB={},"{}",{}'.format(self._mqtt_id, topic, qos)
        return self._send_at(cmd, "OK", 5000) is not None

    def mqtt_unsubscribe(self, topic):
        cmd = 'AT+CMQUNSUB={},"{}"'.format(self._mqtt_id, topic)
        return self._send_at(cmd, "OK", 5000) is not None

    def mqtt_publish(self, topic, payload, qos=1, retain=0):
        """
        Публикация. Payload — строка JSON. SIM7020 требует hex-кодирование.
        """
        hex_payload = "".join("{:02X}".format(b) for b in payload.encode("utf-8"))
        length = len(payload.encode("utf-8"))
        cmd = 'AT+CMQPUB={},"{}",{},{},0,{},"{}"'.format(
            self._mqtt_id, topic, qos, retain, length, hex_payload)
        return self._send_at(cmd, "OK", 10_000) is not None

    def mqtt_wait_msg(self, timeout_ms):
        """
        Ждёт входящее сообщение MQTT (URC +CMQPUB:).

        Формат URC: +CMQPUB: <id>,"<topic>",<qos>,<retain>,<dup>,<len>,"<hex_data>"

        :return: (topic, payload_str) или None по таймауту.
        """
        deadline = utime.ticks_add(utime.ticks_ms(), timeout_ms)
        raw = self._read_until(deadline, terminator=b"+CMQPUB:")
        if b"+CMQPUB:" not in raw:
            return None

        # Дочитываем до конца строки
        deadline2 = utime.ticks_add(utime.ticks_ms(), 1000)
        while b"\r\n" not in raw[raw.index(b"+CMQPUB:"):]:
            if utime.ticks_diff(deadline2, utime.ticks_ms()) <= 0:
                break
            if self.uart.any():
                raw += self.uart.read()
            else:
                utime.sleep_ms(20)

        try:
            line = raw[raw.index(b"+CMQPUB:"):].decode("utf-8", "ignore")
            line = line.split("\r\n")[0]
            # +CMQPUB: 0,"/device/x/config",1,0,0,42,"7B22..."
            parts = line.split(",")
            topic = parts[1].strip().strip('"')
            hex_data = parts[-1].strip().strip('"')
            payload = bytes.fromhex(hex_data).decode("utf-8", "ignore")
            return (topic, payload)
        except Exception as e:
            _log("Failed to parse +CMQPUB: {}".format(e))
            return None

    def mqtt_disconnect(self):
        if self._mqtt_id < 0:
            return
        try:
            self._send_at("AT+CMQDISCON={}".format(self._mqtt_id), "OK", 5000)
        except Exception:
            pass
        self._mqtt_id = -1
