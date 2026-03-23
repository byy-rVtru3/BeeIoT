from machine import UART, Pin
import time

# --- НАСТРОЙКИ ---
# Укажите пины, к которым подключен модуль
# Для ESP32-S3 пины можно выбирать почти любые свободные
TX_PIN = 17  # Пин ESP32, который идет в RX модуля
RX_PIN = 18  # Пин ESP32, который идет в TX модуля

# Скорость UART. SIM800L обычно работает на 9600 или имеет автоопределение.
# Если не работает, попробуйте 115200 или 38400.
BAUD_RATE = 9600

# Инициализация UART
# Используем UART1 (id=1)
uart = UART(1, baudrate=BAUD_RATE, tx=Pin(TX_PIN), rx=Pin(RX_PIN), timeout=2000)


def send_at_command(command, wait_time=1):
    """
    Функция отправки AT-команды и чтения ответа.
    """
    print(f"\n---> Отправка: {command}")
    uart.write(command + '\r\n')
    time.sleep(wait_time)

    if uart.any():
        try:
            # Читаем все данные из буфера
            response = uart.read().decode('utf-8')
            print(f"<--- Ответ:\n{response}")
            return response
        except Exception as e:
            print(f"Ошибка декодирования: {e}")
            return None
    else:
        print("<--- Нет ответа от модуля")
        return None


def main():
    print("Запуск теста SIM800L на ESP32-S3...")
    time.sleep(2)  # Даем модулю время на старт после подачи питания

    # 1. Простая проверка связи (AT)
    # Должен вернуть OK
    send_at_command('AT')

    # 2. Проверка статуса SIM-карты
    # Если READY - карта видна и не требует PIN
    send_at_command('AT+CPIN?')

    # 3. Проверка уровня сигнала
    # Ответ +CSQ: <rssi>,<ber>
    # rssi должен быть от 10 до 31. Если 99 — нет сигнала.
    send_at_command('AT+CSQ')

    # 4. Проверка регистрации в сети
    # Ответ +CREG: 0,1 (домашняя сеть) или 0,5 (роуминг) — это хорошо.
    # Если 0,2 — поиск, 0,0 — не зарегистрирован.
    send_at_command('AT+CREG?')

    # 5. Узнать имя оператора
    send_at_command('AT+COPS?')


if __name__ == '__main__':
    main()