# -*- coding: utf-8 -*-
"""
wifi_transport.py — Резервный канал по WiFi+MQTT.

Используется когда модем не смог подняться/зарегистрироваться (нет NB-IoT-симки,
нет покрытия и т.п.). Интерфейс полностью совпадает с SIM7020, чтобы main.py
мог дёргать его как drop-in.

Зависит от стандартной MicroPython-библиотеки umqtt.simple
(если её нет: mip.install('umqtt.simple') один раз с подключённым WiFi).
"""

import utime
import config


def _log(msg):
    if config.DEBUG:
        print("[WIFI]", msg)


class WiFiMQTT:
    def __init__(self, *args, **kwargs):
        # сигнатура совместима с SIM7020(uart_id, tx, rx, ...) — игнорим всё
        self._wlan = None
        self._client = None
        self._inbox = []

    # ===================================================================
    # Жизненный цикл (mimic SIM7020)
    # ===================================================================

    def power_on(self):
        if not getattr(config, 'WIFI_ENABLED', False):
            _log("disabled in config")
            return False
        if not config.WIFI_SSID:
            _log("WIFI_SSID empty")
            return False
        import network
        # Жёсткая переинициализация — после deepsleep драйвер может застрять
        # в "Internal Error". Несколько попыток с reset драйвера между ними.
        for attempt in range(3):
            try:
                self._wlan = network.WLAN(network.STA_IF)
                # Если активен — выключаем чтобы стряхнуть стейт
                try:
                    self._wlan.active(False)
                    utime.sleep_ms(200)
                except Exception:
                    pass
                self._wlan.active(True)
                utime.sleep_ms(200)
                _log("connecting to {} (try {})".format(config.WIFI_SSID, attempt + 1))
                self._wlan.connect(config.WIFI_SSID, config.WIFI_PASSWORD)
                deadline = utime.ticks_add(utime.ticks_ms(), config.WIFI_CONNECT_TIMEOUT_MS)
                while not self._wlan.isconnected():
                    if utime.ticks_diff(deadline, utime.ticks_ms()) <= 0:
                        raise Exception("connect timeout")
                    utime.sleep_ms(200)
                _log("connected: {}".format(self._wlan.ifconfig()))
                return True
            except Exception as e:
                _log("power_on failed (try {}): {}".format(attempt + 1, e))
                if attempt < 2:
                    utime.sleep_ms(2000)
        return False

    def power_off(self):
        try:
            if self._wlan:
                self._wlan.active(False)
        except Exception:
            pass
        self._wlan = None

    def attach_network(self):
        # для WiFi нечего attach'ить — всё уже в power_on
        return self._wlan is not None and self._wlan.isconnected()

    def signal_strength(self):
        """Возвращает уровень сигнала 0–100% (-1 если недоступно)."""
        try:
            rssi = self._wlan.status('rssi')
            # rssi обычно от -100 (плохо) до -30 (отлично)
            pct = max(0, min(100, 2 * (rssi + 100)))
            return pct
        except Exception:
            return -1

    # Кеш для NTP: один раз синхронизировались — больше не дёргаем.
    # Если упало — не пробуем 5 минут (роутер может блокировать порт 123).
    _ntp_synced = False
    _ntp_failed_until = 0

    @staticmethod
    def _http_time():
        """
        Fallback: время из HTTP Date-header. Работает даже если роутер
        режет UDP 123. Любой публичный сайт через TCP 80 подходит.
        Возвращает Unix-секунды или None.
        """
        import socket
        MONTHS = {b"Jan":1, b"Feb":2, b"Mar":3, b"Apr":4, b"May":5, b"Jun":6,
                  b"Jul":7, b"Aug":8, b"Sep":9, b"Oct":10, b"Nov":11, b"Dec":12}
        for host in ("www.google.com", "www.cloudflare.com", "www.microsoft.com"):
            s = None
            try:
                addr = socket.getaddrinfo(host, 80, 0, socket.SOCK_STREAM)[0][-1]
                s = socket.socket()
                s.settimeout(5)
                s.connect(addr)
                s.send(b"HEAD / HTTP/1.0\r\nHost: " + host.encode() + b"\r\nConnection: close\r\n\r\n")
                buf = b""
                while len(buf) < 2048:
                    chunk = s.recv(256)
                    if not chunk:
                        break
                    buf += chunk
                    if b"\r\n\r\n" in buf:
                        break
                for line in buf.split(b"\r\n"):
                    if line[:5].lower() == b"date:":
                        # "Thu, 11 May 2026 14:30:00 GMT"
                        parts = line[5:].strip().split(b" ")
                        if len(parts) < 5:
                            continue
                        day = int(parts[1])
                        month = MONTHS.get(parts[2], 0)
                        year = int(parts[3])
                        hms = parts[4].split(b":")
                        h, m, sec = int(hms[0]), int(hms[1]), int(hms[2])
                        return utime.mktime((year, month, day, h, m, sec, 0, 0)) + 946_684_800
            except Exception as e:
                _log("http_time {} failed: {}".format(host, e))
            finally:
                if s is not None:
                    try:
                        s.close()
                    except Exception:
                        pass
        return None

    @staticmethod
    def _set_rtc(unix_ts):
        """Прошивает машинный RTC по Unix-секундам (чтобы выжило deepsleep)."""
        try:
            import machine
            tm = utime.gmtime(unix_ts - 946_684_800)
            # (year, month, day, weekday, hour, min, sec, subsec)
            machine.RTC().datetime((tm[0], tm[1], tm[2], tm[6], tm[3], tm[4], tm[5], 0))
        except Exception as e:
            _log("set_rtc failed: {}".format(e))

    def network_time(self):
        """Возвращает UNIX-секунды или None. Кешируется чтобы не лагать каждый цикл."""
        if WiFiMQTT._ntp_synced:
            return utime.time() + 946_684_800
        if utime.ticks_diff(WiFiMQTT._ntp_failed_until, utime.ticks_ms()) > 0:
            return None
        # 1) Сначала NTP (UDP 123)
        try:
            import ntptime
            ntptime.timeout = 2
            ntptime.settime()
            WiFiMQTT._ntp_synced = True
            _log("time via NTP")
            return utime.time() + 946_684_800
        except Exception as e:
            _log("ntp failed: {} — trying HTTP Date".format(e))
        # 2) Fallback на HTTP Date-header (TCP 80)
        http_ts = WiFiMQTT._http_time()
        if http_ts:
            WiFiMQTT._set_rtc(http_ts)
            WiFiMQTT._ntp_synced = True
            _log("time via HTTP Date: {}".format(http_ts))
            return http_ts
        # 3) Оба упали — 5 минут карантина
        _log("ntp+http both failed (retry in 5min)")
        WiFiMQTT._ntp_failed_until = utime.ticks_add(utime.ticks_ms(), 300_000)
        return None

    # ===================================================================
    # MQTT
    # ===================================================================

    def mqtt_connect(self, broker, port, client_id, keepalive=60, user="", password=""):
        try:
            from umqtt.simple import MQTTClient
        except ImportError:
            _log("umqtt.simple not installed (mip.install('umqtt.simple'))")
            return False
        # Несколько попыток с растущей паузой — брокер после нашего deepsleep
        # может не успеть отпустить прошлую сессию, плюс мобильный hotspot
        # любит обрывать TCP в самый неподходящий момент.
        for attempt in range(3):
            try:
                self._client = MQTTClient(
                    client_id, broker, port=port,
                    user=user or None,
                    password=password or None,
                    keepalive=keepalive,
                )
                self._client.set_callback(self._on_message)
                self._client.connect()
                _log("MQTT connected to {}:{}".format(broker, port))
                return True
            except Exception as e:
                _log("MQTT connect failed (try {}): {}".format(attempt + 1, e))
                self._client = None
                if attempt < 2:
                    utime.sleep_ms(3000 * (attempt + 1))   # 3s, 6s
        return False

    def _on_message(self, topic, msg):
        try:
            self._inbox.append((topic.decode("utf-8", "ignore"),
                                msg.decode("utf-8", "ignore")))
        except Exception:
            pass

    def mqtt_subscribe(self, topic, qos=1):
        if not self._client:
            return False
        try:
            self._client.subscribe(topic, qos)
            return True
        except Exception as e:
            _log("subscribe failed: {}".format(e))
            return False

    def mqtt_unsubscribe(self, topic):
        # umqtt.simple не умеет unsubscribe — игнорим
        return True

    def mqtt_publish(self, topic, payload, qos=1, retain=0):
        if not self._client:
            return False
        try:
            self._client.publish(topic, payload, retain=bool(retain), qos=qos)
            return True
        except Exception as e:
            _log("publish failed: {}".format(e))
            return False

    def mqtt_wait_msg(self, timeout_ms):
        """Ждёт входящее сообщение. (topic, payload) или None."""
        if not self._client:
            return None
        deadline = utime.ticks_add(utime.ticks_ms(), timeout_ms)
        while utime.ticks_diff(deadline, utime.ticks_ms()) > 0:
            try:
                self._client.check_msg()
            except Exception as e:
                _log("check_msg failed: {}".format(e))
                return None
            if self._inbox:
                return self._inbox.pop(0)
            utime.sleep_ms(100)
        return None

    def mqtt_disconnect(self):
        try:
            if self._client:
                self._client.disconnect()
        except Exception:
            pass
        self._client = None
