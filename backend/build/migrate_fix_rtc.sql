-- Миграция: чинит записи temperature/noise/weight у которых recorded_at
-- застрял в "далёком прошлом" из-за сбитого RTC на датчике (NTP не дошёл,
-- питание отключали → utime.time() начался с 2000-01-01).
--
-- Логика: для каждого hub_id находим максимальный битый recorded_at —
-- это соответствует моменту "почти сейчас" (последняя запись перед отправкой
-- буфера). Сдвигаем ВСЕ битые записи этого hub'а на (NOW() - max_bad),
-- сохраняя относительные интервалы между ними.
--
-- Запуск (на сервере, в контейнере с postgres):
--   docker compose exec postgres psql -U postgres -d beeiot -f /docker-entrypoint-initdb.d/migrate_fix_rtc.sql
-- или скопировать локально и:
--   psql -h 62.109.16.63 -U postgres -d beeiot -f migrate_fix_rtc.sql

BEGIN;

-- TEMPERATURE
WITH bad_max AS (
    SELECT hub_id, MAX(recorded_at) AS max_ts
    FROM temperature
    WHERE recorded_at < '2024-01-01'
    GROUP BY hub_id
)
UPDATE temperature t
SET recorded_at = t.recorded_at + (NOW() - bm.max_ts)
FROM bad_max bm
WHERE t.hub_id = bm.hub_id AND t.recorded_at < '2024-01-01';

-- NOISE
WITH bad_max AS (
    SELECT hub_id, MAX(recorded_at) AS max_ts
    FROM noise
    WHERE recorded_at < '2024-01-01'
    GROUP BY hub_id
)
UPDATE noise n
SET recorded_at = n.recorded_at + (NOW() - bm.max_ts)
FROM bad_max bm
WHERE n.hub_id = bm.hub_id AND n.recorded_at < '2024-01-01';

-- WEIGHT (на всякий случай)
WITH bad_max AS (
    SELECT hub_id, MAX(recorded_at) AS max_ts
    FROM weight
    WHERE recorded_at < '2024-01-01'
    GROUP BY hub_id
)
UPDATE weight w
SET recorded_at = w.recorded_at + (NOW() - bm.max_ts)
FROM bad_max bm
WHERE w.hub_id = bm.hub_id AND w.recorded_at < '2024-01-01';

COMMIT;

-- Контроль: после миграции эти запросы должны вернуть 0
SELECT 'temperature' AS tbl, COUNT(*) FROM temperature WHERE recorded_at < '2024-01-01'
UNION ALL
SELECT 'noise', COUNT(*) FROM noise WHERE recorded_at < '2024-01-01'
UNION ALL
SELECT 'weight', COUNT(*) FROM weight WHERE recorded_at < '2024-01-01';
