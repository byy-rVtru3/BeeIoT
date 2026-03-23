# -*- coding: utf-8 -*-
"""
network/sim800l.py — Драйвер для GSM-модуля SIM800L.

Реализует:
  - Управление питанием (power_on / power_off)
  - Подключение к GPRS через AT-команды
  - MQTT поверх TCP (ручное формирование MQTT-пакетов по спецификации v3.1.1)

Все публичные методы:
  - Не бросают исключений наружу (всё в try/except)
  - Используют таймауты из config (нет бесконечных while)
  - Возвращают bool (успех/неудача) или данные
"""

import machine
import utime
import ujson
import struct

import config


def _log(msg):
    if config.DEBUG:
        print("[SIM800L]", msg)


# ============================================================
# Константы MQTT v3.1.1 (типы пакетов)
# ============================================================
_MQTT_CONNECT     = 0x10
_MQTT_CONNACK     = 0x20
_MQTT_PUBLISH     = 0x30
_MQTT_PUBACK      = 0x40
_MQTT_SUBSCRIBE   = 0x82
_MQTT_SUBACK      = 0x90
_MQTT_PINGREQ     = 0xC0
_MQTT_PINGRESP    = 0xD0
_MQTT_DISCONNECT  = 0xE0


class SIM800L:
    """
    Драйвер SIM800L: AT-команды, GPRS, MQTT поверх TCP.

    Пример использования:
        sim = SIM800L(config.SIM800L_UART_ID, config.SIM800L_TX_PIN,
                      config.SIM800L_RX_PIN, config.SIM800L_RST_PIN,
                      config.SIM800L_BAUDRATE)
        if sim.power_on():
            if sim.connect_gprs():
                if sim.mqtt_connect(...):
                    sim.mqtt_publish(topic, payload)
                    msg = sim.mqtt_wait_msg(5000)
                    sim.mqtt_disconnect()
        sim.power_off()
    """

    def __init__(self, uart_id: int, tx: int, rx: int, rst: int, baudrate: int):
        """
        :param uart_id:  ID UART-порта (config.SIM800L_UART_ID)
        :param tx:       GPIO пин TX ESP32 → RX SIM800L
        :param rx:       GPIO пин RX ESP32 → TX SIM800L
        :param rst:      GPIO пин RST (или -1 если не используется)
        :param baudrate: Скорость UART (обычно 9600)
        """
        self._uart = machine.UART(uart_id, baudrate=baudrate,
                                  tx=tx, rx=rx,
                                  timeout=2000)
        self._rst_pin = None
        if rst >= 0:
            try:
                self._rst_pin = machine.Pin(rst, machine.Pin.OUT, value=1)
            except Exception as e:
                _log("WARN: RST пин недоступен: {}".format(e))

        self._packet_id = 0    # Счётчик MQTT Packet ID

    # ==================================================================
    # Управление питанием
    # ==================================================================

    def power_on(self) -> bool:
        """
        Включает SIM800L и ждёт регистрации в сети.

        Алгоритм:
          1. Проверяет отклик AT (таймаут SIM800L_POWER_ON_TIMEOUT_MS)
          2. Опрашивает AT+CREG? до получения ответа ",1" или ",5"
             (таймаут GPRS_CONNECT_TIMEOUT_MS)

        :return: True если модуль зарегистрирован в сети, False при таймауте
        """
        try:
            _log("Включение модуля...")

            # Аппаратный сброс модуля (как в BeeiotHub)
            if self._rst_pin is not None:
                _log("Аппаратный сброс (RST)...")
                self._rst_pin.value(0)
                utime.sleep_ms(100)
                self._rst_pin.value(1)
                utime.sleep_ms(5000)

            # Дождаться отклика AT
            deadline = utime.ticks_add(utime.ticks_ms(),
                                       config.SIM800L_POWER_ON_TIMEOUT_MS)
            while utime.ticks_diff(deadline, utime.ticks_ms()) > 0:
                resp = self._send_at("AT", "OK", 1000)
                if resp is not None:
                    _log("Модуль откликнулся")
                    break
                utime.sleep_ms(500)
            else:
                _log("ОШИБКА: модуль не откликнулся")
                return False

            # Отключить эхо AT-команд (как в рабочей BeeiotHub прошивке)
            self._send_at("ATE0", "OK", 2000)

            # Проверка SIM-карты
            _log("Проверка SIM-карты...")
            resp = self._send_at("AT+CPIN?", "+CPIN:", 5000)
            if resp is None:
                _log("ОШИБКА: SIM-карта не отвечает")
                return False
            if "READY" not in resp:
                if "SIM PIN" in resp:
                    _log("ОШИБКА: SIM требует PIN-код. Отключите PIN через телефон")
                elif "NOT INSERTED" in resp:
                    _log("ОШИБКА: SIM-карта не вставлена")
                else:
                    _log("ОШИБКА: SIM статус: {}".format(resp.strip()[:60]))
                return False
            _log("SIM-карта OK")

            # Проверка уровня сигнала (диагностика)
            resp = self._send_at("AT+CSQ", "+CSQ:", 3000)
            if resp and "+CSQ:" in resp:
                try:
                    part = resp.split("+CSQ:")[1].split(",")[0].strip()
                    csq = int(part)
                    if csq == 0:
                        _log("ВНИМАНИЕ: НЕТ СИГНАЛА (CSQ=0). Проверьте антенну!")
                    elif csq == 99:
                        _log("ВНИМАНИЕ: сигнал не определён (CSQ=99)")
                    else:
                        _log("Сигнал: CSQ={} ({}%)".format(csq, int(csq * 100 / 31)))
                except Exception:
                    pass

            # Ждать регистрации в сети
            _log("Ожидание сети (макс. {}с)...".format(
                config.GPRS_CONNECT_TIMEOUT_MS // 1000))
            deadline = utime.ticks_add(utime.ticks_ms(),
                                       config.GPRS_CONNECT_TIMEOUT_MS)
            while utime.ticks_diff(deadline, utime.ticks_ms()) > 0:
                resp = self._send_at("AT+CREG?", "+CREG:",
                                     config.SIM800L_AT_DEFAULT_TIMEOUT_MS)
                if resp and (",1" in resp or ",5" in resp):
                    _log("Зарегистрирован в сети")
                    # Стабилизация после регистрации (как в BeeiotHub)
                    utime.sleep_ms(5000)
                    return True
                utime.sleep_ms(2000)

            _log("ОШИБКА: не зарегистрирован в сети (таймаут)")
            return False
        except Exception as e:
            _log("ОШИБКА power_on: {}".format(e))
            return False

    def power_off(self) -> None:
        """
        Выключает SIM800L. Гарантированно не бросает исключение.

        Отправляет AT+CPOWD=1, ждёт SIM800L_POWER_OFF_WAIT_MS мс.
        Если настроен RST-пин — опускает его.
        """
        try:
            _log("Выключение модуля...")
            self._send_at("AT+CPOWD=1", "NORMAL POWER DOWN",
                          config.SIM800L_POWER_OFF_WAIT_MS)
        except Exception:
            pass
        try:
            utime.sleep_ms(config.SIM800L_POWER_OFF_WAIT_MS)
        except Exception:
            pass
        try:
            if self._rst_pin is not None:
                self._rst_pin.value(0)
                utime.sleep_ms(200)
                self._rst_pin.value(1)
        except Exception:
            pass
        _log("Модуль выключен")

    # ==================================================================
    # GPRS
    # ==================================================================

    def connect_gprs(self) -> bool:
        """
        Поднимает GPRS-соединение через APN.

        Последовательность AT-команд для SIM800L Bearer:
          AT+SAPBR=3,1,"Contype","GPRS"
          AT+SAPBR=3,1,"APN","<APN>"
          AT+SAPBR=3,1,"USER","<user>"    (если задан)
          AT+SAPBR=3,1,"PWD","<pwd>"      (если задан)
          AT+SAPBR=1,1   → открыть
          AT+SAPBR=2,1   → получить IP

        :return: True если получен IP-адрес, False при ошибке
        """
        try:
            _log("Подключение GPRS...")
            t = config.GPRS_AT_TIMEOUT_MS

            self._send_at('AT+SAPBR=3,1,"Contype","GPRS"', "OK", t)
            self._send_at('AT+SAPBR=3,1,"APN","{}"'.format(config.APN), "OK", t)
            if config.APN_USER:
                self._send_at('AT+SAPBR=3,1,"USER","{}"'.format(config.APN_USER),
                              "OK", t)
            if config.APN_PASSWORD:
                self._send_at('AT+SAPBR=3,1,"PWD","{}"'.format(config.APN_PASSWORD),
                              "OK", t)

            resp = self._send_at("AT+SAPBR=1,1", "OK", t)
            if resp is None:
                _log("ОШИБКА: SAPBR открытие не удалось")
                return False

            # Проверяем наличие IP
            resp = self._send_at("AT+SAPBR=2,1", "+SAPBR:", t)
            if resp and "0.0.0.0" not in resp and "+SAPBR: 1,1" in resp:
                _log("GPRS подключён: {}".format(resp))
                return True
            _log("ОШИБКА: нет IP после GPRS ({})".format(resp))
            return False
        except Exception as e:
            _log("ОШИБКА connect_gprs: {}".format(e))
            return False

    # ==================================================================
    # TCP-соединение
    # ==================================================================

    def _tcp_open(self, host: str, port: int) -> bool:
        """Открывает TCP-соединение к host:port через AT+CIPSTART."""
        try:
            t = config.MQTT_CONNECT_TIMEOUT_MS
            self._send_at("AT+CIPSHUT", "SHUT OK", t)
            self._send_at("AT+CIPMUX=0", "OK", config.SIM800L_AT_DEFAULT_TIMEOUT_MS)
            self._send_at("AT+CIPMODE=0", "OK", config.SIM800L_AT_DEFAULT_TIMEOUT_MS)
            resp = self._send_at(
                'AT+CIPSTART="TCP","{}","{}"'.format(host, port),
                "CONNECT", t)
            if resp and "CONNECT" in resp and "FAIL" not in resp:
                _log("TCP подключён к {}:{}".format(host, port))
                return True
            _log("ОШИБКА TCP CIPSTART: {}".format(resp))
            return False
        except Exception as e:
            _log("ОШИБКА _tcp_open: {}".format(e))
            return False

    def _tcp_send(self, data: bytes) -> bool:
        """Отправляет сырые байты через AT+CIPSEND."""
        try:
            cmd = "AT+CIPSEND={}".format(len(data))
            # Ждём приглашение '>'
            resp = self._send_at(cmd, ">", config.SIM800L_AT_DEFAULT_TIMEOUT_MS)
            if resp is None:
                _log("ОШИБКА CIPSEND: нет приглашения '>'")
                return False
            self._uart.write(data)
            # Ждём SEND OK
            result = self._read_until("SEND OK", config.SIM800L_AT_DEFAULT_TIMEOUT_MS)
            if result and "SEND OK" in result:
                return True
            _log("ОШИБКА CIPSEND: нет SEND OK")
            return False
        except Exception as e:
            _log("ОШИБКА _tcp_send: {}".format(e))
            return False

    # ==================================================================
    # MQTT — подключение
    # ==================================================================

    def mqtt_connect(self, broker: str, port: int, client_id: str,
                     user: str = "", password: str = "",
                     keepalive: int = 60) -> bool:
        """
        Подключается к MQTT-брокеру: открывает TCP, отправляет CONNECT, ждёт CONNACK.

        :return: True при успехе
        """
        try:
            if not self._tcp_open(broker, port):
                return False

            pkt = self._build_connect(client_id, user, password, keepalive)
            if not self._tcp_send(pkt):
                return False

            # Читаем CONNACK (4 байта: 0x20 0x02 0x00 0x00)
            resp = self._read_bytes(4, config.MQTT_CONNECT_TIMEOUT_MS)
            if resp and len(resp) >= 4 and resp[0] == _MQTT_CONNACK:
                rc = resp[3]
                if rc == 0:
                    _log("MQTT подключён к {}:{}".format(broker, port))
                    return True
                _log("MQTT CONNACK ошибка, код: {}".format(rc))
                return False
            _log("ОШИБКА: нет CONNACK (получено: {})".format(resp))
            return False
        except Exception as e:
            _log("ОШИБКА mqtt_connect: {}".format(e))
            return False

    # ==================================================================
    # MQTT — публикация
    # ==================================================================

    def mqtt_publish(self, topic: str, payload: str, qos: int = 1) -> bool:
        """
        Публикует сообщение в топик.

        При QoS=1 ждёт PUBACK с таймаутом MQTT_CONNECT_TIMEOUT_MS.

        :return: True при успехе (или QoS=0)
        """
        try:
            self._packet_id = (self._packet_id + 1) & 0xFFFF
            pkt = self._build_publish(topic, payload, qos, self._packet_id)
            if not self._tcp_send(pkt):
                return False

            if qos == 0:
                return True

            # Ждём PUBACK (4 байта: 0x40 0x02 id_msb id_lsb)
            resp = self._read_bytes(4, config.MQTT_CONNECT_TIMEOUT_MS)
            if resp and len(resp) >= 4 and resp[0] == _MQTT_PUBACK:
                _log("PUBACK получен для topic={}".format(topic))
                return True
            _log("ОШИБКА: нет PUBACK (получено: {})".format(resp))
            return False
        except Exception as e:
            _log("ОШИБКА mqtt_publish: {}".format(e))
            return False

    # ==================================================================
    # MQTT — подписка
    # ==================================================================

    def mqtt_subscribe(self, topic: str, qos: int = 1) -> bool:
        """
        Подписывается на топик.

        :return: True при получении SUBACK
        """
        try:
            self._packet_id = (self._packet_id + 1) & 0xFFFF
            pkt = self._build_subscribe(topic, qos, self._packet_id)
            if not self._tcp_send(pkt):
                return False

            resp = self._read_bytes(5, config.MQTT_CONNECT_TIMEOUT_MS)
            if resp and len(resp) >= 5 and resp[0] == _MQTT_SUBACK:
                _log("SUBACK получен для topic={}".format(topic))
                return True
            _log("ОШИБКА: нет SUBACK")
            return False
        except Exception as e:
            _log("ОШИБКА mqtt_subscribe: {}".format(e))
            return False

    # ==================================================================
    # MQTT — ожидание входящего сообщения
    # ==================================================================

    def mqtt_wait_msg(self, timeout_ms: int) -> dict | None:
        """
        Ожидает входящее PUBLISH сообщение.

        Не блокирует дольше timeout_ms. Обрабатывает PINGREQ.

        :param timeout_ms: Максимальное время ожидания в мс
        :return: Распарсенный dict или None (таймаут / ошибка / не JSON)
        """
        try:
            deadline = utime.ticks_add(utime.ticks_ms(), timeout_ms)
            while utime.ticks_diff(deadline, utime.ticks_ms()) > 0:
                if self._uart.any():
                    first_byte = self._uart.read(1)
                    if not first_byte:
                        utime.sleep_ms(50)
                        continue
                    ptype = first_byte[0] & 0xF0

                    if ptype == _MQTT_PUBLISH & 0xF0:
                        payload_str = self._read_publish_payload(first_byte[0])
                        if payload_str:
                            try:
                                return ujson.loads(payload_str)
                            except Exception:
                                _log("Невалидный JSON в /config: {}".format(
                                    payload_str[:60]))
                        return None

                    elif first_byte[0] == _MQTT_PINGREQ:
                        # Отвечаем PINGRESP
                        self._tcp_send(bytes([_MQTT_PINGRESP, 0x00]))

                utime.sleep_ms(100)

            _log("Таймаут ожидания /config ({} мс)".format(timeout_ms))
            return None
        except Exception as e:
            _log("ОШИБКА mqtt_wait_msg: {}".format(e))
            return None

    # ==================================================================
    # MQTT — отключение
    # ==================================================================

    def mqtt_disconnect(self) -> None:
        """Отправляет MQTT DISCONNECT и закрывает TCP-соединение."""
        try:
            self._tcp_send(bytes([_MQTT_DISCONNECT, 0x00]))
            utime.sleep_ms(200)
        except Exception:
            pass
        try:
            self._send_at("AT+CIPCLOSE", "CLOSE OK",
                          config.SIM800L_AT_DEFAULT_TIMEOUT_MS)
        except Exception:
            pass

    # ==================================================================
    # AT-команды — вспомогательные методы
    # ==================================================================

    def _send_at(self, cmd: str, expected: str, timeout_ms: int):
        """
        Отправляет AT-команду и читает ответ до timeout_ms.

        :return: Строка ответа если 'expected' найден, иначе None
        """
        try:
            # Полностью очищаем UART буфер перед отправкой
            while self._uart.any():
                self._uart.read()
            line = "{}\r\n".format(cmd)
            self._uart.write(line)
            _log("→ {}".format(cmd))
            return self._read_until(expected, timeout_ms)
        except Exception as e:
            _log("ОШИБКА _send_at({}): {}".format(cmd, e))
            return None

    def _read_until(self, expected: str, timeout_ms: int):
        """Читает UART до появления ожидаемой подстроки или таймаута."""
        buf = b""
        deadline = utime.ticks_add(utime.ticks_ms(), timeout_ms)
        while utime.ticks_diff(deadline, utime.ticks_ms()) > 0:
            chunk = self._uart.read(64)
            if chunk:
                buf += chunk
                decoded = buf.decode("utf-8", "ignore")
                if expected in decoded:
                    _log("← {}".format(decoded.strip()[:80]))
                    return decoded
            else:
                utime.sleep_ms(10)
        return None

    def _read_bytes(self, count: int, timeout_ms: int):
        """Читает ровно count байт с таймаутом."""
        buf = b""
        deadline = utime.ticks_add(utime.ticks_ms(), timeout_ms)
        while utime.ticks_diff(deadline, utime.ticks_ms()) > 0:
            chunk = self._uart.read(count - len(buf))
            if chunk:
                buf += chunk
            if len(buf) >= count:
                return buf
            utime.sleep_ms(10)
        return buf if buf else None

    # ==================================================================
    # Построение MQTT-пакетов (формат v3.1.1)
    # ==================================================================

    @staticmethod
    def _encode_remaining_length(length: int) -> bytes:
        """Кодирует Remaining Length по алгоритму MQTT (до 4 байт)."""
        result = b""
        while True:
            byte = length & 0x7F
            length >>= 7
            if length > 0:
                byte |= 0x80
            result += bytes([byte])
            if length == 0:
                break
        return result

    @staticmethod
    def _encode_str(s: str) -> bytes:
        """Кодирует строку в MQTT-формат: 2 байта длины + UTF-8 данные."""
        enc = s.encode("utf-8")
        return struct.pack("!H", len(enc)) + enc

    def _build_connect(self, client_id: str, user: str, password: str,
                       keepalive: int) -> bytes:
        """Формирует MQTT CONNECT пакет."""
        # Переменный заголовок
        protocol = self._encode_str("MQTT")
        proto_level = b"\x04"  # MQTT 3.1.1

        connect_flags = 0x02  # CleanSession=1
        if user:
            connect_flags |= 0x80
        if password:
            connect_flags |= 0x40

        var_header = (protocol + proto_level
                      + bytes([connect_flags])
                      + struct.pack("!H", keepalive))

        # Полезная нагрузка
        payload = self._encode_str(client_id)
        if user:
            payload += self._encode_str(user)
        if password:
            payload += self._encode_str(password)

        remaining = var_header + payload
        return (bytes([_MQTT_CONNECT])
                + self._encode_remaining_length(len(remaining))
                + remaining)

    def _build_publish(self, topic: str, payload: str,
                       qos: int, packet_id: int) -> bytes:
        """Формирует MQTT PUBLISH пакет."""
        enc_payload = payload.encode("utf-8")
        var_header = self._encode_str(topic)
        if qos > 0:
            var_header += struct.pack("!H", packet_id)

        flags = _MQTT_PUBLISH | (qos << 1)
        remaining = var_header + enc_payload
        return (bytes([flags])
                + self._encode_remaining_length(len(remaining))
                + remaining)

    def _build_subscribe(self, topic: str, qos: int, packet_id: int) -> bytes:
        """Формирует MQTT SUBSCRIBE пакет."""
        var_header = struct.pack("!H", packet_id)
        payload = self._encode_str(topic) + bytes([qos])
        remaining = var_header + payload
        return (bytes([_MQTT_SUBSCRIBE])
                + self._encode_remaining_length(len(remaining))
                + remaining)

    def _read_publish_payload(self, first_byte: int) -> str | None:
        """
        Читает тело PUBLISH пакета (вызывается после чтения первого байта 0x3x).

        Декодирует Remaining Length, пропускает заголовок топика, возвращает payload.
        """
        try:
            # Читаем Remaining Length (до 4 байт, Variable Byte Integer)
            remaining_length = 0
            multiplier = 1
            for _ in range(4):
                b = self._read_bytes(1, 1000)
                if not b:
                    return None
                remaining_length += (b[0] & 0x7F) * multiplier
                multiplier *= 128
                if not (b[0] & 0x80):
                    break

            # Читаем весь остаток пакета
            packet = self._read_bytes(remaining_length,
                                      config.MQTT_CONNECT_TIMEOUT_MS)
            if not packet or len(packet) < 2:
                return None

            # Первые 2 байта — длина topic string
            topic_len = struct.unpack("!H", packet[0:2])[0]
            # Пропускаем тему
            offset = 2 + topic_len
            
            # QoS (1-2 биты первого байта)
            qos = (first_byte >> 1) & 0x03
            
            # Packet ID (2 байта) присутствует только если QoS > 0
            if qos > 0:
                offset += 2

            payload_bytes = packet[offset:]
            return payload_bytes.decode("utf-8", "ignore")
        except Exception as e:
            _log("ОШИБКА _read_publish_payload: {}".format(e))
            return None
