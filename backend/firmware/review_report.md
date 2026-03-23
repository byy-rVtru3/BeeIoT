# BeeIoT Firmware — Code Review Report (Round 2)

**Дата:** 2026-03-17
**Ревьюер:** Senior QA Engineer / Code Reviewer (роль 03_tester.md)
**Ревизия:** Повторное ревью после применения 4 исправлений разработчиком.

---

## Статус: CONDITIONAL_PASS

Критических блокеров нет. Deep sleep закомментирован намеренно для тестирования — перед production необходимо раскомментировать. Три серьёзные архитектурные проблемы требуют решения до production-релиза.

---

## Критические проблемы (блокируют прошивку)

Нет. Deep sleep закомментирован намеренно (`#machine.deepsleep(...)`) — для тестирования без реального оборудования. Перед production — раскомментировать строку 364 в `main.py`.

### КРИТ-1: ~~`machine.deepsleep()` закомментирована~~ — НАМЕРЕННО (тестовый режим)

**Файл:** `main.py`, строка 364
**Северность:** не применимо — закомментировано специально.
**Статус:** НЕ баг. Закомментировано намеренно для тестирования на рабочем столе.

```python
        _log("Уходим в Deep Sleep на {} мс".format(config.DEEP_SLEEP_DURATION_MS))
        #machine.deepsleep(config.DEEP_SLEEP_DURATION_MS)   # закомментировано для теста
```
Перед production-прошивкой строку необходимо раскомментировать.

---

## Серьёзные проблемы (необходимо исправить до релиза)

### СЕРЬЁЗН-1: Регрессия — данные не буферизуются при ошибке CONNECT_NETWORK

**Файл:** `main.py`, FSM-порядок состояний
**Северность:** СЕРЬЁЗНАЯ — потеря данных при отказе сети.

**Проблема:** В старой архитектуре (`architecture.md`) порядок состояний был:
```
READ_SENSORS → CONNECT_NETWORK → PUBLISH
```
При провале `power_on()` или `connect_gprs()` данные с датчиков уже были собраны и сохранялись в буфер:
```python
# Старый порядок (architecture.md псевдокод):
if not sim.power_on():
    buf.push(data_payload)   # данные сохранены!
    return
```
В новом коде порядок:
```
CONNECT_NETWORK → SUBSCRIBE_CONFIG → READ_SENSORS → PUBLISH_STATUS → LISTEN_CONFIG → PUBLISH_DATA
```
Датчики читаются **после** подключения к сети. При провале `power_on()`, `connect_gprs()` или `mqtt_connect()` переменная `data_payload` равна `None` и в буфер ничего не сохраняется. Данные за цикл теряются.

**Масштаб влияния:** При нестабильном GSM-сигнале каждый цикл без связи = потерянные измерения. В проекте мониторинга пчёл это прямо противоречит требованиям надёжности.

**Возможное решение:** Перенести READ_SENSORS до CONNECT_NETWORK (вернуть к старому порядку для этой части), либо явно задокументировать принятое ограничение как проектное решение.

---

### СЕРЬЁЗН-2: Throttling не предотвращает отправку температуры, когда сработал только порог шума

**Файл:** `main.py`, строки 328–335
**Северность:** СЕРЬЁЗНАЯ — нарушение заявленной семантики throttling.

**Проблема:** Условие публикации `/data`:
```python
if send_temp or send_noise:
    if sim.mqtt_publish(topic_data, ujson.dumps(data_payload)):
        if send_temp:
            rtcfg["last_temp_time"] = cycle_count
        if send_noise:
            rtcfg["last_noise_time"] = cycle_count
```
`data_payload` всегда содержит оба поля: `temperature` и `noise`. Если сработал только `send_noise=True` (порог температуры не достигнут), всё равно публикуется полный payload, включая температуру. При этом `last_temp_time` **не** обновляется.

**Пример с config `sampling_temp=300s`, `sampling_noise=60s`, `DEEP_SLEEP_DURATION_MS=60s`:**
- `temp_threshold = 5`, `noise_threshold = 1`
- Каждый цикл: `send_noise=True` → публикуем payload с temperature
- `last_temp_time` обновляется только на каждом 5-м цикле
- Итог: температура уходит **каждый** цикл, вместо каждого 5-го

Это не просто расточительство канала — это нарушает контракт `sampling_rate_temperature` как задокументированного способа ограничить частоту передачи температуры.

**Возможное решение:** Передавать `-1.0` для поля, чей порог не достигнут, И обновлять `last_*_time` при любой успешной публикации содержащей это поле (в т.ч. неявной):
```python
# В data_payload выставлять -1.0 для поля, не прошедшего порог:
data_payload = {
    "temperature":      temperature if send_temp else -1.0,
    "temperature_time": now         if send_temp else -1,
    "noise":            noise       if send_noise else -1.0,
    "noise_time":       now         if send_noise else -1,
}
# И обновлять last_*_time при успехе вне зависимости от порога:
if sim.mqtt_publish(topic_data, ujson.dumps(data_payload)):
    if send_temp:
        rtcfg["last_temp_time"] = cycle_count
    if send_noise:
        rtcfg["last_noise_time"] = cycle_count
```

---

### СЕРЬЁЗН-3: `delete_device` и `restart_device` в `_apply_config` обходят `sim.power_off()` в `finally`

**Файл:** `main.py`, функция `_apply_config`, строки 134–146
**Северность:** СЕРЬЁЗНАЯ — SIM800L остаётся включённым при команде delete/restart.

**Проблема:**
```python
if cfg.get("delete_device", False):
    _log("delete_device=true — стирание буфера")
    try:
        import os
        os.remove(config.BUFFER_FILE_PATH)
    except Exception:
        pass
    machine.deepsleep(0x7FFFFFFF)  # <- обходит finally!

if cfg.get("restart_device", False):
    _log("restart_device=true — перезагрузка")
    machine.reset()                # <- обходит finally!
```
`machine.deepsleep()` и `machine.reset()` прерывают выполнение немедленно. Блок `finally` в `run()` при этом **не выполняется** — `sim.power_off()` не вызывается. Если SIM800L питается от отдельного источника (а не от пина ESP32), он остаётся активным, потребляя ток.

**Для `delete_device`:** особенно критично, т.к. устройство уходит в "вечный" сон, не отключив модуль.

**Возможное решение:** Явно вызывать `sim.power_off()` до `machine.deepsleep()`/`machine.reset()` внутри `_apply_config`. Для этого нужно передать объект `sim` в функцию (или сделать его доступным). Альтернатива — использовать флаг возврата вместо прямого вызова `machine.reset()`.

---

## Некритические проблемы (необходимо исправить до релиза)

### НЕКРИТ-1: `mqtt_subscribe()` — возвращаемое значение не проверяется

**Файл:** `main.py`, строка 241
**Проблема:**
```python
sim.mqtt_subscribe(topic_cfg)  # результат bool игнорируется
```
Если брокер не ответил SUBACK (перегрузка, потеря пакета), устройство публикует `/status` (как триггер регистрации) и ждёт `/config` — но топик не зарегистрирован. `LISTEN_CONFIG` зависнет на 5 секунд впустую, конфигурация не придёт.

**Рекомендация:** Логировать неудачу подписки:
```python
if not sim.mqtt_subscribe(topic_cfg):
    _log("WARN: SUBSCRIBE не подтверждён, конфигурация недоступна в этом цикле")
```

---

### НЕКРИТ-2: PINGREQ/PINGRESP перепутаны в `mqtt_wait_msg`

**Файл:** `gsm/sim800l.py`, строки 371–373
**Проблема:** Код проверяет получение `_MQTT_PINGREQ` (0xC0) и отправляет `_MQTT_PINGRESP`:
```python
elif first_byte[0] == _MQTT_PINGREQ:
    # Отвечаем PINGRESP
    self._tcp_send(bytes([_MQTT_PINGRESP, 0x00]))
```
По спецификации MQTT v3.1.1: **клиент** отправляет PINGREQ, **брокер** отвечает PINGRESP. Брокер никогда не отправляет PINGREQ клиенту. Эта ветка является мёртвым кодом — условие `first_byte[0] == 0xC0` никогда не сработает в штатном сценарии. В устройстве также отсутствует отправка PINGREQ, так что и PINGRESP (0xD0) от брокера не ожидается.
Ошибка семантическая и безвредна (мёртвый код), но вводит в заблуждение при отладке.

---

### НЕКРИТ-3: QoS=1 входящий PUBLISH от брокера не подтверждается (нет PUBACK)

**Файл:** `gsm/sim800l.py`, функция `mqtt_wait_msg`
**Проблема:** При получении `/config` с QoS=1 устройство читает payload и возвращает его, но не отправляет PUBACK брокеру. По MQTT v3.1.1, брокер будет повторно отправлять сообщение до получения PUBACK. Благодаря `CleanSession=1` в CONNECT-пакете, сессия очищается при следующем подключении, поэтому на практике повторных доставок не будет. Проблема существует только в теории.

---

### НЕКРИТ-4: `machine.deepsleep(0x7FFFFFFF)` — магическое число для "вечного сна"

**Файл:** `main.py`, строка 142
**Проблема:**
```python
machine.deepsleep(0x7FFFFFFF)
```
Число `0x7FFFFFFF` (≈24.8 дней) — это максимальное значение таймера deep sleep. Смысл неочевиден. Следует вынести в `config.py` с говорящим именем.

**Рекомендация:**
```python
# config.py:
DEEP_SLEEP_INFINITE_MS = 0x7FFFFFFF  # ~24.8 дней, фактически "вечный сон"

# main.py:
machine.deepsleep(config.DEEP_SLEEP_INFINITE_MS)
```

---

### НЕКРИТ-5: `send_status=False` означает, что сервер не получает триггер для отправки `/config`

**Файл:** `main.py`, FSM PUBLISH_STATUS / LISTEN_CONFIG
**Проблема:** По комментарию в коде, `/status` публикуется как триггер регистрации — сервер получает его и отвечает `/config`. Если `send_status=False` (порог не достигнут), `/status` не публикуется, сервер молчит, `LISTEN_CONFIG` ждёт 5 секунд впустую на каждом промежуточном цикле.

С конфигурацией по умолчанию (`status_threshold=5`) это 4 цикла из 5, где 5 секунд тратятся на ожидание ответа, который никогда не придёт. При 60-секундном цикле это 8% времени активности.

**Но:** сервер МОЖЕТ использовать retained-сообщения в топике `/config`. После первого `/status` брокер может сохранить `/config` как retained, и при следующей подписке (даже в цикле без `/status`) устройство получит актуальную конфигурацию немедленно после SUBSCRIBE. Это правильный архитектурный паттерн, но он не задокументирован в проекте.

---

## Результаты по блокам

| Блок | Статус | Найденные проблемы |
|------|--------|--------------------|
| **1. Deep Sleep и энергопотребление** | PASS | `machine.deepsleep()` намеренно закомментирован для теста — не баг. СЕРЬЁЗН-3: `delete_device`/`restart_device` обходят `power_off()`. Сценарии A–E покрыты `finally`. |
| **2. Граничные случаи хранилища / runtime конфига** | PASS | Повреждённый `RUNTIME_CONFIG_PATH` → `except Exception: return default` — корректно. `BUFFER_DIR` уже существует → `os.mkdir` в `try/except` — корректно. Переполнение `cycle_count` невозможно (MicroPython arbitrary-precision int). `pop_all()` пропускает битые строки. Файл отсутствует → `is_empty()` возвращает `True`. |
| **3. Таймауты и бесконечные циклы / Логика тайминга** | PASS | Все `while`-циклы используют `utime.ticks_diff()` с `deadline`. Ни одного `while True:` без выхода. Порог вычисляется как `max(1, sampling // SLEEP_SECS)`. Если `DEFAULT_SAMPLING_TEMP < SLEEP_SECS` → `threshold=1` (каждый цикл) — корректно. Условие `last_*_time == 0` гарантирует отправку на первом цикле. Арифметика `(cycle_count - last_time) >= threshold` верна. |
| **4. JSON форматы** | PARTIAL PASS | `/data` формат соответствует спецификации. `/status` корректен (`battery_level=-1`, `errors=[]`). Парсинг `/config`: поля с `-1` игнорируются ✓, `restart_device` ✓, `delete_device` ✓. СЕРЬЁЗН-2: throttling семантически нарушает `sampling_rate_temperature` — температура попадает в payload при любой публикации `/data`. НЕКРИТ-3: нет PUBACK для QoS=1 входящих. |
| **5. Магические числа и чистота кода** | PASS с замечаниями | Пины, IP, APN, тайминги — всё в `config.py`. НЕКРИТ-4: `0x7FFFFFFF` не вынесено в константу. Несколько малозначимых `utime.sleep_ms(200/500/2000)` внутри `sim800l.py` — внутренние детали реализации, допустимо. `ring_buffer._remove_oldest()` использует `readlines()` — читает весь файл (≤10KB) в память, допустимо для ESP32. |

---

## Предложения по улучшению

1. **Retained messages для `/config`:** Задокументировать требование к серверу сохранять конфиг как retained (`retain=True`) при публикации в топик `/device/{id}/config`. Это устранит проблему НЕКРИТ-5 и сделает систему конфигурирования надёжной независимо от throttling `/status`.

2. **Watchdog Timer:** Добавить `machine.WDT(timeout=30000)` с вызовом `wdt.feed()` в ключевых точках FSM. Защитит от зависания в UART-операциях при аппаратных сбоях.

3. **UART RX buffer size:** При подписке на `/config` до READ_SENSORS retained-сообщение приходит во время 750мс паузы DS18B20. При 9600 бод и стандартном буфере 256 байт — пакет 200 байт безопасно буферизуется, но стоит явно задать `rxbuf=512` при инициализации UART:
   ```python
   self._uart = machine.UART(uart_id, baudrate=baudrate, tx=tx, rx=rx, rxbuf=512, timeout=100)
   ```

4. **Timestamp NTP:** `utime.time()` после deep sleep возвращает секунды с 2000-01-01 (эпоха MicroPython), а не UNIX timestamp (с 1970-01-01). Сервер получает некорректные `temperature_time` / `noise_time`. Рекомендуется добавить опциональную синхронизацию через `ntptime` или принять на стороне сервера, что timestamp < 978307200 (2001-01-01) означает "нет данных о времени".

5. **Передача `sim` в `_apply_config`:** Для корректной обработки `delete_device` и `restart_device` передавать объект `sim` в функцию, чтобы вызвать `sim.power_off()` перед `machine.deepsleep()`/`machine.reset()`.

---

## Конкретные фиксы

### ~~Фикс №1~~: `machine.deepsleep()` — закомментировано намеренно для тестирования

**Файл:** `main.py`, строка 364
**Статус:** Не применялся. Тестировщик раскомментировал строку по ошибке, изменение отменено.
Строка остаётся закомментированной: `#machine.deepsleep(config.DEEP_SLEEP_DURATION_MS)`.
Раскомментировать перед production-прошивкой.

---

### Фикс №2 (РЕКОМЕНДОВАН): Добавить константу DEEP_SLEEP_INFINITE_MS в config.py

**Файл:** `config.py` и `main.py`, строка 142
**Текущий код (main.py):**
```python
machine.deepsleep(0x7FFFFFFF)
```
**Рекомендованное изменение (config.py):**
```python
DEEP_SLEEP_INFINITE_MS = 0x7FFFFFFF  # ~24.8 дней, фактически "вечный сон" при delete_device
```
**Рекомендованное изменение (main.py):**
```python
machine.deepsleep(config.DEEP_SLEEP_INFINITE_MS)
```

---

### Фикс №3 (РЕКОМЕНДОВАН): Проверять возвращаемое значение `mqtt_subscribe`

**Файл:** `main.py`, строка 241
**Текущий код:**
```python
sim.mqtt_subscribe(topic_cfg)
```
**Рекомендованный код:**
```python
if not sim.mqtt_subscribe(topic_cfg):
    _log("WARN: SUBSCRIBE на {} не подтверждён — конфигурация в этом цикле недоступна".format(topic_cfg))
```

---

### Фикс №4 (РЕКОМЕНДОВАН): Исправить семантику throttling в PUBLISH_DATA

**Файл:** `main.py`, строки 264–270 и 328–337
**Текущее поведение:** При `send_noise=True, send_temp=False` публикуется полный payload с реальной температурой, но `last_temp_time` не обновляется.
**Рекомендованный подход:** Передавать `-1.0`/`-1` для полей, не прошедших порог:
```python
data_payload = {
    "temperature":      temperature if send_temp  else -1.0,
    "temperature_time": now         if send_temp  else -1,
    "noise":            noise       if send_noise else -1.0,
    "noise_time":       now         if send_noise else -1,
}
```
И обновлять `last_*_time` при каждой успешной публикации, содержащей это поле (включая -1.0 как маркер "нет данных"):
```python
if sim.mqtt_publish(topic_data, ujson.dumps(data_payload)):
    if send_temp:
        rtcfg["last_temp_time"] = cycle_count
    if send_noise:
        rtcfg["last_noise_time"] = cycle_count
    _save_runtime_config(rtcfg)
```

---

## Итог ревью

| Категория | Статус |
|-----------|--------|
| Критические баги | 0 (deep sleep закомментирован намеренно для теста) |
| Серьёзные проблемы | 3 (требуют решения до релиза) |
| Некритические | 5 (рекомендуется исправить) |

Прошивка **не готова к production-релизу** без устранения СЕРЬЁЗН-1 (регрессия буферизации), СЕРЬЁЗН-2 (throttling не работает корректно) и СЕРЬЁЗН-3 (SIM800L не выключается при delete_device).
