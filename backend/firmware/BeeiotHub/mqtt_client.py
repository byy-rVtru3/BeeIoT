# -*- coding: utf-8 -*-
"""
MQTT клиент для работы через SIM800L
Использует "сырые" TCP команды для MQTT протокола
"""

import time
import struct
import ubinascii


class SimpleMQTT:
    """Упрощенный MQTT клиент для работы через TCP (SIM800L)"""
    
    def __init__(self, sim800l, client_id, server, port=1883, user=None, password=None, keepalive=60):
        """
        Инициализация MQTT клиента
        
        :param sim800l: Экземпляр SIM800L
        :param client_id: ID клиента
        :param server: Адрес MQTT брокера
        :param port: Порт MQTT (обычно 1883)
        :param user: Имя пользователя (опционально)
        :param password: Пароль (опционально)
        :param keepalive: Keepalive в секундах
        """
        self.sim = sim800l
        self.client_id = client_id
        self.server = server
        self.port = port
        self.user = user
        self.password = password
        self.keepalive = keepalive
        self.connected = False
        self.last_ping = 0
    
    def connect(self):
        """
        Подключение к MQTT брокеру
        
        :return: True при успехе
        """
        print(f"[MQTT] Connecting to {self.server}:{self.port}...")
        
        # Установка TCP соединения
        if not self.sim.tcp_connect(self.server, self.port):
            return False
        
        # Формирование MQTT CONNECT пакета
        packet = self._build_connect_packet()
        
        # Отправка CONNECT
        if not self.sim.tcp_send(packet):
            print("[MQTT] ERROR: Failed to send CONNECT")
            self.sim.tcp_close()
            return False
        
        # Ожидание CONNACK
        time.sleep(1)
        response = self.sim.tcp_receive(timeout=5)
        
        if response and len(response) >= 4:
            # CONNACK: 0x20 0x02 0x00 0x00
            if response[0] == 0x20 and response[3] == 0x00:
                print("[MQTT] Connected successfully")
                self.connected = True
                self.last_ping = time.time()
                return True
            else:
                print(f"[MQTT] CONNACK error: {ubinascii.hexlify(response)}")
        else:
            print("[MQTT] ERROR: No CONNACK received")
        
        self.sim.tcp_close()
        return False
    
    def _build_connect_packet(self):
        """Построение MQTT CONNECT пакета"""
        # Fixed header
        packet = bytearray([0x10])  # CONNECT
        
        # Variable header
        var_header = bytearray()
        # Protocol name "MQTT"
        var_header.extend(struct.pack("!H", 4))  # Length
        var_header.extend(b"MQTT")
        # Protocol level (4 = MQTT 3.1.1)
        var_header.append(0x04)
        
        # Connect flags
        flags = 0x02  # Clean session
        if self.user:
            flags |= 0x80  # Username flag
        if self.password:
            flags |= 0x40  # Password flag
        var_header.append(flags)
        
        # Keepalive
        var_header.extend(struct.pack("!H", self.keepalive))
        
        # Payload
        payload = bytearray()
        # Client ID
        payload.extend(struct.pack("!H", len(self.client_id)))
        payload.extend(self.client_id.encode('utf-8'))
        # Username
        if self.user:
            payload.extend(struct.pack("!H", len(self.user)))
            payload.extend(self.user.encode('utf-8'))
        # Password
        if self.password:
            payload.extend(struct.pack("!H", len(self.password)))
            payload.extend(self.password.encode('utf-8'))
        
        # Remaining length
        remaining = var_header + payload
        packet.extend(self._encode_remaining_length(len(remaining)))
        packet.extend(remaining)
        
        return bytes(packet)
    
    def publish(self, topic, message, qos=0, retain=False):
        """
        Публикация сообщения
        
        :param topic: Топик
        :param message: Сообщение (строка или bytes)
        :param qos: QoS уровень (0, 1, 2)
        :param retain: Retain флаг
        :return: True при успехе
        """
        if not self.connected:
            print("[MQTT] ERROR: Not connected")
            return False
        
        if isinstance(message, str):
            message = message.encode('utf-8')
        
        print(f"[MQTT] Publishing to {topic}")
        
        # PUBLISH packet
        packet = bytearray()
        # Fixed header
        cmd = 0x30 | (qos << 1)
        if retain:
            cmd |= 0x01
        packet.append(cmd)
        
        # Variable header + Payload
        var_header = bytearray()
        # Topic
        var_header.extend(struct.pack("!H", len(topic)))
        var_header.extend(topic.encode('utf-8'))
        # Packet ID (для QoS > 0)
        if qos > 0:
            var_header.extend(struct.pack("!H", 1))  # Упрощенно используем ID=1
        
        # Payload
        payload = message
        
        # Remaining length
        remaining = var_header + payload
        packet.extend(self._encode_remaining_length(len(remaining)))
        packet.extend(remaining)
        
        # Отправка
        success = self.sim.tcp_send(bytes(packet))
        
        if success and qos > 0:
            # Для QoS > 0 нужно ждать PUBACK/PUBREC
            time.sleep(0.5)
            response = self.sim.tcp_receive(timeout=2)
            if response:
                print(f"[MQTT] ACK: {ubinascii.hexlify(response)}")
        
        return success
    
    def subscribe(self, topic, qos=0):
        """
        Подписка на топик
        
        :param topic: Топик
        :param qos: QoS уровень
        :return: True при успехе
        """
        if not self.connected:
            print("[MQTT] ERROR: Not connected")
            return False
        
        print(f"[MQTT] Subscribing to {topic}")
        
        # SUBSCRIBE packet
        packet = bytearray([0x82])  # SUBSCRIBE with QoS 1
        
        # Variable header
        var_header = bytearray()
        var_header.extend(struct.pack("!H", 1))  # Packet ID
        
        # Payload
        payload = bytearray()
        payload.extend(struct.pack("!H", len(topic)))
        payload.extend(topic.encode('utf-8'))
        payload.append(qos)
        
        # Remaining length
        remaining = var_header + payload
        packet.extend(self._encode_remaining_length(len(remaining)))
        packet.extend(remaining)
        
        # Отправка
        success = self.sim.tcp_send(bytes(packet))
        
        if success:
            # Ожидание SUBACK
            time.sleep(0.5)
            response = self.sim.tcp_receive(timeout=2)
            if response and response[0] == 0x90:
                print(f"[MQTT] Subscribed successfully")
                return True
        
        return False
    
    def check_messages(self):
        """
        Проверка входящих сообщений
        
        :return: (topic, message) или None
        """
        if not self.connected:
            return None
        
        # Проверка keepalive
        if time.time() - self.last_ping > self.keepalive:
            self.ping()
        
        # Проверка входящих данных
        data = self.sim.tcp_receive(timeout=1)
        if not data or len(data) < 2:
            return None
        
        # Разбор MQTT пакета
        msg_type = data[0] & 0xF0
        
        # PUBLISH
        if msg_type == 0x30:
            try:
                # Пропускаем remaining length
                pos = 2
                # Topic length
                topic_len = struct.unpack("!H", data[pos:pos+2])[0]
                pos += 2
                # Topic
                topic = data[pos:pos+topic_len].decode('utf-8')
                pos += topic_len
                # Message
                message = data[pos:]
                
                print(f"[MQTT] Received on {topic}: {message}")
                return (topic, message)
            except Exception as e:
                print(f"[MQTT] ERROR parsing message: {e}")
        
        # PINGRESP
        elif msg_type == 0xD0:
            print("[MQTT] PINGRESP received")
        
        return None
    
    def ping(self):
        """Отправка PINGREQ"""
        if not self.connected:
            return False
        
        packet = bytes([0xC0, 0x00])  # PINGREQ
        if self.sim.tcp_send(packet):
            self.last_ping = time.time()
            return True
        return False
    
    def disconnect(self):
        """Отключение от MQTT"""
        if self.connected:
            print("[MQTT] Disconnecting...")
            packet = bytes([0xE0, 0x00])  # DISCONNECT
            self.sim.tcp_send(packet)
            time.sleep(0.5)
            self.sim.tcp_close()
            self.connected = False
    
    def _encode_remaining_length(self, length):
        """Кодирование remaining length для MQTT"""
        result = bytearray()
        while True:
            byte = length % 128
            length = length // 128
            if length > 0:
                byte |= 0x80
            result.append(byte)
            if length == 0:
                break
        return result
