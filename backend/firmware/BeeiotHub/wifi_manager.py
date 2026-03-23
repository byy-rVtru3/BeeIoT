# -*- coding: utf-8 -*-
"""
WiFi Manager для ESP32
Управление WiFi подключением
"""

import network
import time


class WiFiManager:
    """Класс для управления WiFi подключением"""

    def __init__(self, ssid, password, timeout=15):
        """
        Инициализация WiFi менеджера

        :param ssid: Имя WiFi сети
        :param password: Пароль WiFi
        :param timeout: Таймаут подключения в секундах
        """
        self.ssid = ssid
        self.password = password
        self.timeout = timeout
        self.wlan = None
        self.connected = False

    def connect(self):
        """
        Подключение к WiFi сети

        :return: True при успешном подключении
        """
        print(f"[WiFi] Connecting to WiFi: {self.ssid}")

        try:
            # Создание WiFi интерфейса
            print("[WiFi] Activating WiFi interface...")
            self.wlan = network.WLAN(network.STA_IF)

            # Деактивация и повторная активация для сброса состояния
            if self.wlan.active():
                print("[WiFi] WiFi was active, deactivating first...")
                self.wlan.active(False)
                time.sleep(1)

            self.wlan.active(True)
            time.sleep(1)
            print("[WiFi] WiFi interface activated")

            # Проверка, уже подключены
            if self.wlan.isconnected():
                print("[WiFi] [OK] Already connected")
                self.connected = True
                ip = self.wlan.ifconfig()[0]
                print(f"[WiFi] IP address: {ip}")
                return True

            # Сканирование доступных сетей (для отладки)
            print("[WiFi] Scanning for available networks...")
            try:
                networks = self.wlan.scan()
                print(f"[WiFi] Found {len(networks)} network(s)")
                found_ssid = False
                for net in networks:
                    ssid = net[0].decode('utf-8') if isinstance(net[0], bytes) else net[0]
                    if ssid == self.ssid:
                        found_ssid = True
                        print(f"[WiFi] [OK] Target network '{self.ssid}' found in scan")
                        break

                if not found_ssid:
                    print(f"[WiFi] [WARNING] Target network '{self.ssid}' not found in scan!")
                    print(f"[WiFi] Available networks:")
                    for net in networks[:5]:  # Показываем первые 5
                        ssid = net[0].decode('utf-8') if isinstance(net[0], bytes) else net[0]
                        rssi = net[3]
                        print(f"[WiFi]   - {ssid} (signal: {rssi} dBm)")
            except Exception as e:
                print(f"[WiFi] [WARNING] Network scan failed: {e}")

            # Подключение к сети
            print(f"[WiFi] Connecting (timeout: {self.timeout}s)...")
            print(f"[WiFi] SSID: {self.ssid}")
            print(f"[WiFi] Password length: {len(self.password)} chars")

            self.wlan.connect(self.ssid, self.password)

            # Ожидание подключения
            start_time = time.time()
            connection_attempts = 0
            while not self.wlan.isconnected():
                elapsed = time.time() - start_time
                if elapsed > self.timeout:
                    print("[WiFi] [FAIL] Connection timeout")

                    # Проверка статуса
                    status = self.wlan.status()
                    print(f"[WiFi] Final status code: {status}")
                    self._print_status_code(status)

                    self.connected = False
                    return False

                connection_attempts += 1
                if connection_attempts % 3 == 0:
                    status = self.wlan.status()
                    print(f"[WiFi] Waiting... (status: {status}, elapsed: {int(elapsed)}s)")

                time.sleep(1)

            # Подключение успешно
            self.connected = True
            ip = self.wlan.ifconfig()[0]
            print(f"[WiFi] [OK] Connected successfully")
            print(f"[WiFi] IP address: {ip}")
            print(f"[WiFi] Netmask: {self.wlan.ifconfig()[1]}")
            print(f"[WiFi] Gateway: {self.wlan.ifconfig()[2]}")
            print(f"[WiFi] DNS: {self.wlan.ifconfig()[3]}")

            return True

        except OSError as e:
            print(f"[WiFi] [FAIL] OS Error: {e}")
            print("[WiFi] Possible causes:")
            print("[WiFi]   - Wrong password")
            print("[WiFi]   - Network out of range")
            print("[WiFi]   - WiFi module hardware issue")
            self.connected = False
            return False
        except Exception as e:
            print(f"[WiFi] [FAIL] Connection error: {e}")
            import sys
            sys.print_exception(e)
            self.connected = False
            return False

    def _print_status_code(self, status):
        """Расшифровка кода статуса WiFi"""
        status_messages = {
            0: "STAT_IDLE - no connection and no activity",
            1: "STAT_CONNECTING - connecting in progress",
            2: "STAT_WRONG_PASSWORD - failed due to incorrect password",
            3: "STAT_NO_AP_FOUND - failed because no access point replied",
            4: "STAT_CONNECT_FAIL - failed due to other problems",
            5: "STAT_GOT_IP - connection successful",
            -1: "STAT_IDLE (initial state)",
            -2: "STAT_CONNECT_FAIL",
            -3: "STAT_NO_AP_FOUND"
        }

        message = status_messages.get(status, f"Unknown status code: {status}")
        print(f"[WiFi] Status: {message}")

        if status == 2:
            print("[WiFi] [ERROR] Wrong WiFi password!")
        elif status == 3:
            print("[WiFi] [ERROR] Access point not found - check SSID!")
        elif status == 4:
            print("[WiFi] [ERROR] Connection failed - check settings!")

    def disconnect(self):
        """Отключение от WiFi"""
        print("[WiFi] Disconnecting...")
        try:
            if self.wlan:
                self.wlan.disconnect()
                self.wlan.active(False)
                self.connected = False
                print("[WiFi] [OK] Disconnected")
        except Exception as e:
            print(f"[WiFi] [WARNING] Disconnect error: {e}")

    def is_connected(self):
        """
        Проверка статуса подключения

        :return: True если подключено
        """
        if self.wlan:
            self.connected = self.wlan.isconnected()
            return self.connected
        return False

    def get_signal_strength(self):
        """
        Получить уровень сигнала WiFi

        :return: Уровень сигнала в dBm или None
        """
        try:
            if self.wlan and self.is_connected():
                rssi = self.wlan.status('rssi')

                # Преобразование dBm в проценты
                # -30 dBm = отличный сигнал (100%)
                # -90 dBm = очень слабый сигнал (0%)
                if rssi <= -90:
                    percent = 0
                elif rssi >= -30:
                    percent = 100
                else:
                    percent = int(((rssi + 90) / 60) * 100)

                print(f"[WiFi] Signal strength: {rssi} dBm ({percent}%)")
                return percent
        except Exception as e:
            print(f"[WiFi] [WARNING] Cannot get signal strength: {e}")

        return -1

    def scan_networks(self):
        """
        Сканирование доступных WiFi сетей

        :return: Список найденных сетей
        """
        print("[WiFi] Scanning for networks...")
        try:
            if not self.wlan:
                self.wlan = network.WLAN(network.STA_IF)
                self.wlan.active(True)

            networks = self.wlan.scan()
            print(f"[WiFi] Found {len(networks)} network(s):")

            for net in networks:
                ssid = net[0].decode('utf-8')
                rssi = net[3]
                print(f"[WiFi]   - {ssid}: {rssi} dBm")

            return networks
        except Exception as e:
            print(f"[WiFi] [WARNING] Scan error: {e}")
            return []

    def get_ip(self):
        """
        Получить IP адрес

        :return: IP адрес или None
        """
        try:
            if self.wlan and self.is_connected():
                return self.wlan.ifconfig()[0]
        except:
            pass
        return None
