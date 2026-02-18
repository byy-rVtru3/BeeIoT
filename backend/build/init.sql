CREATE TABLE users (
                       id SERIAL PRIMARY KEY,
                       email TEXT UNIQUE NOT NULL,
                       name     TEXT NOT NULL,
                       password TEXT NOT NULL
);

CREATE TABLE hives (
                       id SERIAL PRIMARY KEY,
                       user_id INTEGER REFERENCES users(id) ON DELETE CASCADE,
                       name TEXT NOT NULL,
                       temperature_check TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       noise_check TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       sensor_id   TEXT UNIQUE
                       status      BOOLEAN DEFAULT TRUE
);

CREATE TABLE temperature (
                             id SERIAL PRIMARY KEY,
                             hive_id INTEGER REFERENCES hives(id) ON DELETE CASCADE,
                             level FLOAT NOT NULL,
                             recorded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE weight (
                        id SERIAL PRIMARY KEY,
                        hive_id INTEGER REFERENCES hives(id) ON DELETE CASCADE,
                        level FLOAT NOT NULL,
                        recorded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE noise (
                       id SERIAL PRIMARY KEY,
                       hive_id INTEGER REFERENCES hives(id) ON DELETE CASCADE,
                       level FLOAT NOT NULL,
                       recorded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
