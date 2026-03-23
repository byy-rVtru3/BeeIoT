# -*- coding: utf-8 -*-
"""
MQTT клиент для работы через WiFi
Использует стандартную библиотеку umqtt.simple для ESP32
"""

import socket


class WiFiMQTT:
    """MQTT клиент для работы через WiFi"""

    def __init__(self, client_id, server, port=1883, user=None, password=None, keepalive=60, timeout=10):
        """
        Инициализация MQTT клиента

        :param client_id: ID клиента
        :param server: Адрес MQTT брокера
        :param port: Порт MQTT (обычно 1883)
        :param user: Имя пользователя
        :param password: Пароль
        :param keepalive: Keepalive в секундах
        :param timeout: Таймаут подключения в секундах
        """
        self.client_id = client_id
        self.server = server
        self.port = port
        self.user = user
        self.password = password
        self.keepalive = keepalive
        self.timeout = timeout
        self.client = None
        self.connected = False
        self.subscribed_topics = {}

    def _test_connection(self):
        """
        Проверка доступности MQTT брокера через сокет

        :return: True если брокер доступен
        """
        print(f"[WiFi-MQTT] Testing connection to {self.server}:{self.port}...")
        sock = None
        try:
            # Создаем сокет с таймаутом
            sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            sock.settimeout(self.timeout)

            # Пытаемся подключиться
            sock.connect((self.server, self.port))
            sock.close()
            print("[WiFi-MQTT] [OK] Broker is reachable")
            return True

        except OSError as e:
            print(f"[WiFi-MQTT] [FAIL] Broker unreachable: {e}")
            if sock:
                try:
                    sock.close()
                except:
                    pass
            return False

    def connect(self):
        """
        Подключение к MQTT брокеру

        :return: True при успехе
        """
        print(f"[WiFi-MQTT] Connecting to {self.server}:{self.port}...")

        # Сначала проверяем доступность брокера
        if not self._test_connection():
            print("[WiFi-MQTT] [FAIL] Broker is not available, skipping MQTT connect")
            return False

        try:
            # Импортируем umqtt.simple
            from umqtt.simple import MQTTClient

            # Создание MQTT клиента
            self.client = MQTTClient(
                client_id=self.client_id,
                server=self.server,
                port=self.port,
                user=self.user,
                password=self.password,
                keepalive=self.keepalive
            )

            # Установка callback для входящих сообщений
            self.client.set_callback(self._message_callback)

            # Устанавливаем таймаут на сокет клиента
            print(f"[WiFi-MQTT] Setting socket timeout to {self.timeout}s...")

            # Подключение с таймаутом
            self.client.connect()
            self.connected = True
            print("[WiFi-MQTT] [OK] Connected to MQTT broker")

            return True

        except ImportError:
            print("[WiFi-MQTT] [ERROR] umqtt.simple not found!")
            print("[WiFi-MQTT] Install: import upip; upip.install('micropython-umqtt.simple')")
            return False
        except OSError as e:
            print(f"[WiFi-MQTT] [FAIL] Connection timeout or network error: {e}")
            self.connected = False
            return False
        except Exception as e:
            print(f"[WiFi-MQTT] [FAIL] Connection error: {e}")
            self.connected = False
            return False

    def disconnect(self):
        """Отключение от MQTT брокера"""
        print("[WiFi-MQTT] Disconnecting...")
        try:
            if self.client and self.connected:
                self.client.disconnect()
                self.connected = False
                print("[WiFi-MQTT] [OK] Disconnected")
        except Exception as e:
            print(f"[WiFi-MQTT] [WARNING] Disconnect error: {e}")

    def publish(self, topic, payload, qos=0, retain=False):
        """
        Публикация сообщения

        :param topic: Топик
        :param payload: Данные (строка или bytes)
        :param qos: Quality of Service (0, 1, 2)
        :param retain: Флаг retain
        :return: True при успехе
        """
        if not self.connected:
            print("[WiFi-MQTT] [ERROR] Not connected")
            return False

        try:
            if isinstance(payload, str):
                payload = payload.encode('utf-8')

            # ВАЖНО: umqtt.simple надежно поддерживает только QoS 0 и 1
            # QoS 2 может вызывать проблемы, поэтому ограничиваем до QoS 1
            if qos > 1:
                print(f"[WiFi-MQTT] [WARNING] QoS {qos} not fully supported, using QoS 1")
                qos = 1

            # Проверяем соединение перед публикацией
            try:
                self.client.ping()
            except:
                print("[WiFi-MQTT] [WARNING] Connection lost, reconnecting...")
                self.connected = False
                # Пытаемся переподключиться
                if not self.connect():
                    return False

            # Публикуем сообщение
            self.client.publish(topic, payload, qos=qos, retain=retain)
            print(f"[WiFi-MQTT] [OK] Published to {topic} (QoS {qos})")
            return True

        except OSError as e:
            # Сетевая ошибка - помечаем как отключенный
            print(f"[WiFi-MQTT] [FAIL] Network error: {e}")
            self.connected = False
            return False
        except Exception as e:
            # Другие ошибки - не помечаем как отключенный, возможно временная проблема
            print(f"[WiFi-MQTT] [FAIL] Publish error: {e}")
            import sys
            sys.print_exception(e)
            # НЕ помечаем self.connected = False - соединение может быть еще активно
            return False

    def subscribe(self, topic, qos=0):
        """
        Подписка на топик

        :param topic: Топик для подписки
        :param qos: Quality of Service (0, 1, 2)
        :return: True при успехе
        """
        if not self.connected:
            print("[WiFi-MQTT] [ERROR] Not connected")
            return False

        try:
            self.client.subscribe(topic, qos=qos)
            self.subscribed_topics[topic] = qos
            print(f"[WiFi-MQTT] [OK] Subscribed to {topic}")
            return True

        except Exception as e:
            print(f"[WiFi-MQTT] [FAIL] Subscribe error: {e}")
            return False

    def check_messages(self):
        """
        Проверка входящих сообщений

        :return: (topic, payload) или None
        """
        if not self.connected:
            return None

        try:
            # check_msg() не блокирует, возвращает сразу
            self.client.check_msg()
            return None
        except Exception as e:
            print(f"[WiFi-MQTT] [WARNING] Check messages error: {e}")
            return None

    def _message_callback(self, topic, msg):
        """
        Callback для входящих сообщений

        :param topic: Топик
        :param msg: Сообщение
        """
        try:
            topic_str = topic.decode('utf-8')
            msg_str = msg.decode('utf-8')

            print(f"[WiFi-MQTT] Message received:")
            print(f"[WiFi-MQTT]   Topic: {topic_str}")
            print(f"[WiFi-MQTT]   Payload: {msg_str}")

            # Сохраняем последнее сообщение
            self.last_message = (topic_str, msg_str)

        except Exception as e:
            print(f"[WiFi-MQTT] [WARNING] Callback error: {e}")

    def wait_msg(self):
        """
        Ожидание сообщения (блокирующий вызов)

        :return: True если получено сообщение
        """
        if not self.connected:
            return False

        try:
            self.client.wait_msg()
            return True
        except Exception as e:
            print(f"[WiFi-MQTT] [WARNING] Wait message error: {e}")
            return False

    def ping(self):
        """
        Отправка MQTT ping

        :return: True при успехе
        """
        if not self.connected:
            return False

        try:
            self.client.ping()
            return True
        except Exception as e:
            print(f"[WiFi-MQTT] [WARNING] Ping error: {e}")
            self.connected = False
            return False
