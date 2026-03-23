# -*- coding: utf-8 -*-
"""
Драйвер для SIM800L GSM/GPRS модуля
Управление через AT-команды
"""

from machine import UART, Pin
import time


class SIM800L:
    def __init__(self, uart_id, tx_pin, rx_pin, baudrate=9600, rst_pin=None):
        """
        Инициализация SIM800L модуля
        
        :param uart_id: ID UART (обычно 1 или 2)
        :param tx_pin: GPIO для TX (ESP32 -> SIM800L RX)
        :param rx_pin: GPIO для RX (ESP32 <- SIM800L TX)
        :param baudrate: Скорость UART (обычно 9600)
        :param rst_pin: GPIO для RST (опционально)
        """
        self.uart = UART(uart_id, baudrate=baudrate, tx=tx_pin, rx=rx_pin, timeout=2000)  # Increased timeout to 2 seconds
        self.rst_pin = Pin(rst_pin, Pin.OUT) if rst_pin else None
        self.debug = True
        
    def _send_at(self, cmd, expected="OK", timeout=5):
        """
        Отправка AT-команды и ожидание ответа
        
        :param cmd: AT-команда
        :param expected: Ожидаемый ответ
        :param timeout: Таймаут в секундах
        :return: True если получен ожидаемый ответ
        """
        # Clear UART buffer before sending
        while self.uart.any():
            self.uart.read()

        self.uart.write(cmd + "\r\n")
        if self.debug:
            print(f"[SIM800L] >> {cmd}")
        
        start = time.time()
        response = ""
        
        while time.time() - start < timeout:
            if self.uart.any():
                chunk = self.uart.read()
                if chunk:
                    try:
                        response += chunk.decode('utf-8', 'ignore')
                    except:
                        pass
                    
                    if expected in response:
                        if self.debug:
                            print(f"[SIM800L] << {response.strip()}")
                        return True
            time.sleep(0.1)
        
        if self.debug:
            print(f"[SIM800L] << {response.strip()} (TIMEOUT)")
        return False
    
    def _get_response(self, timeout=5):
        """Получить полный ответ от модуля"""
        start = time.time()
        response = ""
        
        while time.time() - start < timeout:
            if self.uart.any():
                chunk = self.uart.read()
                if chunk:
                    try:
                        response += chunk.decode('utf-8', 'ignore')
                    except:
                        pass
            else:
                if response:
                    break
            time.sleep(0.1)
        
        return response.strip()
    
    def reset(self):
        """Аппаратный сброс модуля (если подключен RST пин)"""
        if self.rst_pin:
            print("[SIM800L] Hardware reset...")
            self.rst_pin.value(0)
            time.sleep(0.1)
            self.rst_pin.value(1)
            time.sleep(5)
            return True
        return False
    
    def init(self):
        """Инициализация модуля"""
        print("[SIM800L] Initializing...")
        print("[SIM800L] LED status guide:")
        print("[SIM800L]   - Blink every 1 sec = searching for network")
        print("[SIM800L]   - Blink every 3 sec = registered in network")
        print("[SIM800L]   - Blink every 2 sec = GPRS connected")

        # Проверка связи
        for _ in range(5):
            if self._send_at("AT", "OK", 2):
                break
            time.sleep(1)
        else:
            print("[SIM800L] ERROR: No response to AT")
            return False
        
        # Отключение эха
        self._send_at("ATE0", "OK", 2)

        # Проверка SIM-карты
        print("[SIM800L] Checking SIM card status...")
        self.uart.write("AT+CPIN?\r\n")
        time.sleep(1)
        sim_response = self._get_response(3)
        print(f"[SIM800L] SIM status: {sim_response}")

        if "READY" in sim_response:
            print("[SIM800L] [OK] SIM card is READY")
        elif "SIM PIN" in sim_response:
            print("[SIM800L] [ERROR] SIM card requires PIN code")
            print("[SIM800L] SOLUTION: Disable PIN on SIM card using phone")
            return False
        elif "NOT INSERTED" in sim_response or "ERROR" in sim_response:
            print("[SIM800L] [ERROR] SIM card NOT DETECTED")
            print("[SIM800L] This is why registration fails!")
            print("[SIM800L] SOLUTION:")
            print("[SIM800L]   1. Power OFF the device completely")
            print("[SIM800L]   2. Remove and reinsert SIM card properly")
            print("[SIM800L]   3. Make sure SIM holder is closed")
            print("[SIM800L]   4. Power ON and try again")
            return False
        else:
            print(f"[SIM800L] [WARNING] Unknown SIM status, continuing...")

        # Проверка уровня сигнала
        print("[SIM800L] Checking signal strength...")
        self.uart.write("AT+CSQ\r\n")
        time.sleep(0.5)
        csq_response = self._get_response(2)
        print(f"[SIM800L] Signal: {csq_response}")

        # Разбор ответа CSQ
        if "+CSQ:" in csq_response:
            try:
                csq = int(csq_response.split(":")[1].split(",")[0].strip())
                if csq == 0:
                    print("[SIM800L] [ERROR] NO SIGNAL (CSQ=0)")
                    print("[SIM800L] SOLUTION:")
                    print("[SIM800L]   1. Check GSM antenna is connected")
                    print("[SIM800L]   2. Move device near window or outside")
                    print("[SIM800L]   3. Check antenna cable connection")
                elif csq == 99:
                    print("[SIM800L] [ERROR] SIGNAL NOT DETECTABLE (CSQ=99)")
                    print("[SIM800L] SOLUTION: Antenna problem or no coverage")
                elif csq < 10:
                    signal_percent = int((csq / 31.0) * 100)
                    print(f"[SIM800L] [WARNING] WEAK SIGNAL (CSQ={csq}, {signal_percent}%)")
                    print("[SIM800L] May have trouble connecting to network")
                else:
                    signal_percent = int((csq / 31.0) * 100)
                    print(f"[SIM800L] [OK] Good signal (CSQ={csq}, {signal_percent}%)")
            except:
                print(f"[SIM800L] [WARNING] Could not parse signal strength")

        # Проверка регистрации оператора
        print("[SIM800L] Checking operator registration...")
        self.uart.write("AT+COPS?\r\n")
        time.sleep(1)
        operator_response = self._get_response(3)
        print(f"[SIM800L] Operator: {operator_response}")

        # Ожидание регистрации в сети
        print("[SIM800L] Waiting for network registration (up to 60 sec)...")
        print("[SIM800L] Watch LED: should change from 1sec blink to 3sec blink")
        for attempt in range(30):
            self.uart.write("AT+CREG?\r\n")
            time.sleep(0.5)
            creg_response = self._get_response(2)

            if "+CREG: 0,1" in creg_response or "+CREG: 0,5" in creg_response:
                print(f"[SIM800L] [OK] Registered in network (attempt {attempt+1})")
                print("[SIM800L] [OK] LED should now blink every 3 seconds")
                print("[SIM800L] Waiting 5 seconds for network stabilization...")
                time.sleep(5)  # IMPORTANT: Give module time to stabilize after registration
                return True
            elif "+CREG: 0,2" in creg_response:
                print(f"[SIM800L] Searching for network... (attempt {attempt+1}/30)")
                print(f"[SIM800L] LED is blinking every 1 sec (searching)")
            elif "+CREG: 0,3" in creg_response:
                print(f"[SIM800L] [ERROR] Network registration denied")
                print("[SIM800L] SOLUTION: Check if SIM card is activated")
                return False
            elif "+CREG: 0,0" in creg_response:
                print(f"[SIM800L] Not registered (attempt {attempt+1}/30)")
                print("[SIM800L] LED blinking 1/sec - still searching")
            else:
                print(f"[SIM800L] Network status: {creg_response} (attempt {attempt+1}/30)")

            time.sleep(2)
        
        print("[SIM800L] [ERROR] Network registration timeout (60 seconds)")
        print("[SIM800L] LED still blinking every 1 second = NOT CONNECTED")
        print("[SIM800L] TROUBLESHOOTING:")
        print("[SIM800L] 1. Check SIM card is activated and has credit")
        print("[SIM800L] 2. Check GSM antenna is connected properly")
        print("[SIM800L] 3. Check signal strength (move to better location)")
        print("[SIM800L] 4. Try SIM card in phone to verify it works")
        print("[SIM800L] 5. Check SIM card format (use 2G/3G, not LTE-only)")
        return False
    
    def get_signal_quality(self):
        """
        Получить качество сигнала GSM
        
        :return: Уровень сигнала 0-100% или -1 при ошибке
        """
        self.uart.write("AT+CSQ\r\n")
        time.sleep(0.5)
        response = self._get_response(2)
        
        # Ответ: +CSQ: 15,0  (15 - уровень сигнала 0-31)
        if "+CSQ:" in response:
            try:
                csq = int(response.split(":")[1].split(",")[0].strip())
                if csq == 99:
                    return -1  # Нет сигнала
                # Преобразуем 0-31 в 0-100%
                return int((csq / 31.0) * 100)
            except:
                pass
        
        return -1
    
    def connect_gprs(self, apn, user="", password=""):
        """
        Подключение к GPRS
        
        :param apn: APN оператора
        :param user: Имя пользователя (обычно пусто)
        :param password: Пароль (обычно пусто)
        :return: True при успехе
        """
        print(f"[SIM800L] Connecting to GPRS (APN: {apn})...")
        
        # Проверка, что модуль отвечает
        print("[SIM800L] Testing module responsiveness...")
        if not self._send_at("AT", "OK", 3):
            print("[SIM800L] [ERROR] Module not responding")
            return False

        # Закрыть предыдущее соединение (увеличен таймаут до 65 сек)
        print("[SIM800L] Closing previous connection...")
        print("[SIM800L] This may take up to 65 seconds (SIM800L specification)...")

        # Очищаем буфер перед отправкой
        while self.uart.any():
            self.uart.read()

        # Отправляем команду
        self.uart.write("AT+CIPSHUT\r\n")
        if self.debug:
            print("[SIM800L] >> AT+CIPSHUT")

        # Ждем ответ с увеличенным таймаутом
        start = time.time()
        response = ""
        timeout = 65

        while time.time() - start < timeout:
            if self.uart.any():
                chunk = self.uart.read()
                if chunk:
                    try:
                        response += chunk.decode('utf-8', 'ignore')
                    except:
                        pass

                    # Принимаем любой из возможных ответов
                    if "SHUT OK" in response or "OK" in response or "ERROR" in response:
                        if self.debug:
                            print(f"[SIM800L] << {response.strip()}")
                        print("[SIM800L] [OK] Connection closed")
                        break
            time.sleep(0.1)
        else:
            # Если таймаут, продолжаем (возможно соединение уже было закрыто)
            if self.debug:
                print(f"[SIM800L] << {response.strip()} (TIMEOUT, continuing anyway)")
            print("[SIM800L] [WARN] CIPSHUT timeout, continuing...")

        time.sleep(2)  # Wait after shutdown

        # Настройка режима одиночного соединения
        print("[SIM800L] Setting single connection mode...")
        if not self._send_at("AT+CIPMUX=0", "OK", 5):
            print("[SIM800L] [ERROR] Failed to set connection mode")
            return False
        time.sleep(1)

        # Установка APN
        print(f"[SIM800L] Setting APN: {apn}...")
        apn_cmd = f'AT+CSTT="{apn}","{user}","{password}"'
        if not self._send_at(apn_cmd, "OK", 5):
            print("[SIM800L] [ERROR] Failed to set APN")
            return False
        time.sleep(1)

        # Поднятие беспроводного соединения
        print("[SIM800L] Bringing up wireless connection...")
        print("[SIM800L] This may take 30-85 seconds...")
        if not self._send_at("AT+CIICR", "OK", 85):
            print("[SIM800L] [ERROR] Failed to bring up GPRS connection")
            print("[SIM800L] SOLUTION:")
            print("[SIM800L]   1. Check APN settings are correct")
            print("[SIM800L]   2. Check SIM card has data service enabled")
            print("[SIM800L]   3. Check signal strength is sufficient")
            return False
        time.sleep(2)

        # Получение IP адреса
        print("[SIM800L] Getting IP address...")
        self.uart.write("AT+CIFSR\r\n")
        time.sleep(3)
        ip = self._get_response(5)
        
        if ip and ip != "ERROR" and "." in ip:
            print(f"[SIM800L] [OK] GPRS connected, IP: {ip}")
            print("[SIM800L] LED should blink every 2 seconds (GPRS mode)")
            return True
        
        print("[SIM800L] [ERROR] Failed to get IP address")
        print(f"[SIM800L] Response: {ip}")
        return False
    
    def tcp_connect(self, host, port):
        """
        Установить TCP соединение
        
        :param host: Адрес сервера
        :param port: Порт
        :return: True при успехе
        """
        print(f"[SIM800L] Connecting to {host}:{port}...")
        
        # Начало TCP соединения
        cmd = f'AT+CIPSTART="TCP","{host}","{port}"'
        self.uart.write(cmd + "\r\n")
        
        start = time.time()
        response = ""
        
        while time.time() - start < 30:  # Increased from 15 to 30 seconds
            if self.uart.any():
                chunk = self.uart.read()
                if chunk:
                    try:
                        response += chunk.decode('utf-8', 'ignore')
                    except:
                        pass
                    
                    if "CONNECT OK" in response:
                        print("[SIM800L] TCP connected")
                        return True

                    if "CONNECT FAIL" in response or "ERROR" in response:
                        print(f"[SIM800L] TCP connection failed: {response}")
                        return False

            time.sleep(0.1)
        
        print(f"[SIM800L] TCP connection timeout: {response}")
        return False
    
    def tcp_send(self, data):
        """
        Отправить данные через TCP
        
        :param data: Данные (строка или bytes)
        :return: True при успехе
        """
        if isinstance(data, str):
            data = data.encode('utf-8')
        
        if self.debug:
            print(f"[SIM800L] Sending {len(data)} bytes...")

        # Начало отправки данных
        cmd = f"AT+CIPSEND={len(data)}"
        self.uart.write(cmd + "\r\n")
        time.sleep(0.5)
        
        # Ожидание приглашения '>'
        response = self._get_response(2)
        if ">" not in response:
            print("[SIM800L] ERROR: No prompt for data")
            print(f"[SIM800L] Response: {response}")
            return False
        
        # Отправка данных
        self.uart.write(data)
        time.sleep(0.5)
        
        # Ожидание подтверждения
        response = self._get_response(5)
        if "SEND OK" in response:
            if self.debug:
                print("[SIM800L] Data sent successfully")
            return True
        
        print(f"[SIM800L] Send failed: {response}")
        return False
    
    def tcp_receive(self, timeout=5):
        """
        Получить данные из TCP соединения
        SIM800L отправляет данные в формате: +IPD,<length>:<data>

        :param timeout: Таймаут в секундах
        :return: Полученные данные (только payload) или None
        """
        start = time.time()
        raw_data = b""

        if self.debug:
            print(f"[SIM800L] Waiting for data (timeout: {timeout}s)...")

        while time.time() - start < timeout:
            if self.uart.any():
                chunk = self.uart.read()
                if chunk:
                    raw_data += chunk
                    # Check if we have complete IPD packet
                    if b"+IPD," in raw_data:
                        try:
                            # Parse IPD format: +IPD,<length>:<data>
                            ipd_start = raw_data.find(b"+IPD,")
                            if ipd_start >= 0:
                                # Find the colon separator
                                colon_pos = raw_data.find(b":", ipd_start)
                                if colon_pos > 0:
                                    # Extract length
                                    length_str = raw_data[ipd_start+5:colon_pos].decode('utf-8', 'ignore')
                                    try:
                                        expected_length = int(length_str)
                                        # Extract data after colon
                                        data_start = colon_pos + 1
                                        data_end = data_start + expected_length

                                        # Check if we have all data
                                        if len(raw_data) >= data_end:
                                            payload = raw_data[data_start:data_end]
                                            if self.debug:
                                                print(f"[SIM800L] Received {len(payload)} bytes")
                                            return payload
                                    except ValueError:
                                        pass
                        except Exception as e:
                            if self.debug:
                                print(f"[SIM800L] Parse error: {e}")
            else:
                if raw_data:
                    # Give extra time for complete data
                    time.sleep(0.2)
                    if self.uart.any():
                        continue
                    else:
                        break
            time.sleep(0.1)
        
        if raw_data:
            if self.debug:
                print(f"[SIM800L] Raw data received: {raw_data[:100]}")
            # Try to extract any data even if format is unexpected
            if b"+IPD," in raw_data:
                colon_pos = raw_data.find(b":")
                if colon_pos > 0:
                    payload = raw_data[colon_pos+1:]
                    if payload:
                        if self.debug:
                            print(f"[SIM800L] Extracted {len(payload)} bytes from incomplete IPD")
                        return payload
            return raw_data  # Return raw data as fallback

        if self.debug:
            print("[SIM800L] No data received")
        return None
    
    def tcp_close(self):
        """Закрыть TCP соединение"""
        print("[SIM800L] Closing TCP connection...")
        self._send_at("AT+CIPCLOSE", "CLOSE OK", 5)
        time.sleep(1)
    
    def disconnect_gprs(self):
        """Отключиться от GPRS"""
        print("[SIM800L] Disconnecting GPRS...")
        self._send_at("AT+CIPSHUT", "SHUT OK", 5)
