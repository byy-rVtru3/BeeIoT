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

CREATE TABLE hubs (
                       id SERIAL PRIMARY KEY,
                       email TEXT NOT NULL,
                       name TEXT NOT NULL,
                       sensor TEXT NOT NULL,
                       UNIQUE (email, sensor)
);

CREATE TABLE queens (
                        id SERIAL PRIMARY KEY,
                        email TEXT NOT NULL,
                        name TEXT NOT NULL,
                        start_date DATE NOT NULL,
                        UNIQUE (email, name)
);

CREATE TABLE hives (
                       id SERIAL PRIMARY KEY,
                       user_id INTEGER REFERENCES users(id) ON DELETE CASCADE,
                       name TEXT NOT NULL,
                       temperature_check TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       noise_check TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       sensor_id INTEGER REFERENCES sensors(id),
                       hub_id INTEGER REFERENCES hubs(id),
                       queen_id INTEGER REFERENCES queens(id),
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