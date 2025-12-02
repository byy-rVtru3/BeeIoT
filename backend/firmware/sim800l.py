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
        self.uart = UART(uart_id, baudrate=baudrate, tx=tx_pin, rx=rx_pin, timeout=1000)
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
        if not self._send_at("AT+CPIN?", "READY", 5):
            print("[SIM800L] ERROR: SIM card not ready")
            return False
        
        # Ожидание регистрации в сети
        print("[SIM800L] Waiting for network registration...")
        for _ in range(30):
            if self._send_at("AT+CREG?", "+CREG: 0,1", 2) or \
               self._send_at("AT+CREG?", "+CREG: 0,5", 2):
                print("[SIM800L] Registered in network")
                return True
            time.sleep(2)
        
        print("[SIM800L] ERROR: Network registration timeout")
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
        
        # Закрыть предыдущее соединение
        self._send_at("AT+CIPSHUT", "SHUT OK", 5)
        time.sleep(1)
        
        # Настройка режима одиночного соединения
        if not self._send_at("AT+CIPMUX=0", "OK", 2):
            return False
        
        # Установка APN
        apn_cmd = f'AT+CSTT="{apn}","{user}","{password}"'
        if not self._send_at(apn_cmd, "OK", 5):
            return False
        
        # Поднятие беспроводного соединения
        if not self._send_at("AT+CIICR", "OK", 10):
            return False
        
        # Получение IP адреса
        self.uart.write("AT+CIFSR\r\n")
        time.sleep(2)
        ip = self._get_response(5)
        
        if ip and ip != "ERROR":
            print(f"[SIM800L] GPRS connected, IP: {ip}")
            return True
        
        print("[SIM800L] ERROR: Failed to get IP address")
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
        
        while time.time() - start < 15:
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
                    elif "CONNECT FAIL" in response or "ERROR" in response:
                        print(f"[SIM800L] TCP connection failed: {response}")
                        return False
            time.sleep(0.1)
        
        print("[SIM800L] TCP connection timeout")
        return False
    
    def tcp_send(self, data):
        """
        Отправить данные через TCP
        
        :param data: Данные (строка или bytes)
        :return: True при успехе
        """
        if isinstance(data, str):
            data = data.encode('utf-8')
        
        # Начало отправки данных
        cmd = f"AT+CIPSEND={len(data)}"
        self.uart.write(cmd + "\r\n")
        time.sleep(0.5)
        
        # Ожидание приглашения '>'
        response = self._get_response(2)
        if ">" not in response:
            print("[SIM800L] ERROR: No prompt for data")
            return False
        
        # Отправка данных
        self.uart.write(data)
        time.sleep(0.5)
        
        # Ожидание подтверждения
        response = self._get_response(5)
        if "SEND OK" in response:
            return True
        
        print(f"[SIM800L] Send failed: {response}")
        return False
    
    def tcp_receive(self, timeout=5):
        """
        Получить данные из TCP соединения
        
        :param timeout: Таймаут в секундах
        :return: Полученные данные или None
        """
        start = time.time()
        data = b""
        
        while time.time() - start < timeout:
            if self.uart.any():
                chunk = self.uart.read()
                if chunk:
                    data += chunk
            else:
                if data:
                    break
            time.sleep(0.1)
        
        if data:
            return data
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
