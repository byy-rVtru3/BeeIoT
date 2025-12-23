INSERT INTO users (email, password)
VALUES ('tester@test.com', 'hashed_secret')
ON CONFLICT (email) DO NOTHING;

INSERT INTO hives (user_id, name, sensor_id, temperature_check, noise_check)
VALUES (
    (SELECT id FROM users WHERE email = 'tester@test.com'),
    'Test Hive 1',
    'sensor-123',
    NOW() - INTERVAL '2 days',
    NOW() - INTERVAL '2 days'
)
ON CONFLICT (sensor_id) DO UPDATE SET
    temperature_check = NOW() - INTERVAL '2 days',
    noise_check = NOW() - INTERVAL '2 days';

INSERT INTO temperature (hive_id, level, recorded_at) VALUES
((SELECT id FROM hives WHERE sensor_id = 'sensor-123'), 35.5, NOW() - INTERVAL '2 hours'),
((SELECT id FROM hives WHERE sensor_id = 'sensor-123'), 36.6, NOW() - INTERVAL '1 hour'),
((SELECT id FROM hives WHERE sensor_id = 'sensor-123'), 37.0, NOW() - INTERVAL '30 minutes');

INSERT INTO noise (hive_id, level, recorded_at) VALUES
((SELECT id FROM hives WHERE sensor_id = 'sensor-123'), 45.0, NOW() - INTERVAL '2 hours'),
((SELECT id FROM hives WHERE sensor_id = 'sensor-123'), 50.5, NOW() - INTERVAL '1 hour'),
((SELECT id FROM hives WHERE sensor_id = 'sensor-123'), 55.0, NOW() - INTERVAL '30 minutes');

