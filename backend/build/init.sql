CREATE TABLE users (
                       id SERIAL PRIMARY KEY,
                       email TEXT UNIQUE NOT NULL,
                       name TEXT,
                       password TEXT NOT NULL
);

CREATE TABLE firebase (
                          id SERIAL PRIMARY KEY,
                          user_id INTEGER REFERENCES users(id) ON DELETE CASCADE,
                          token TEXT,
                          device TEXT,
                          UNIQUE (user_id, device)
);

CREATE TABLE sensors (
                         id SERIAL PRIMARY KEY,
                         user_id INTEGER REFERENCES users(id) ON DELETE CASCADE,
                         sensor_id TEXT NOT NULL,
                         active BOOLEAN DEFAULT FALSE,
                         UNIQUE (user_id, sensor_id)
);

CREATE TABLE hives (
                       id SERIAL PRIMARY KEY,
                       user_id INTEGER REFERENCES users(id) ON DELETE CASCADE,
                       name TEXT NOT NULL,
                       temperature_check TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       noise_check TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       sensor_id INTEGER REFERENCES sensors(id),
                       status BOOLEAN DEFAULT TRUE
);
CREATE INDEX ON hives (user_id);

CREATE TABLE temperature (
                             id SERIAL PRIMARY KEY,
                             hive_id INTEGER REFERENCES hives(id) ON DELETE CASCADE,
                             level FLOAT NOT NULL,
                             recorded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                             UNIQUE (hive_id, recorded_at)
);

CREATE TABLE weight (
                        id SERIAL PRIMARY KEY,
                        hive_id INTEGER REFERENCES hives(id) ON DELETE CASCADE,
                        level FLOAT NOT NULL,
                        recorded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        UNIQUE (hive_id, recorded_at)
);

CREATE TABLE noise (
                       id SERIAL PRIMARY KEY,
                       hive_id INTEGER REFERENCES hives(id) ON DELETE CASCADE,
                       level FLOAT NOT NULL,
                       recorded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       UNIQUE (hive_id, recorded_at)
);