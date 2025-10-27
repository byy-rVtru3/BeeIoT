CREATE TABLE users (
                       id SERIAL PRIMARY KEY,
                       username VARCHAR(255) UNIQUE NOT NULL,
                       password_hash VARCHAR(255) NOT NULL,
                       email VARCHAR(255) UNIQUE NOT NULL,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE hives (
                       id SERIAL PRIMARY KEY,
                       user_id INTEGER REFERENCES users(id) ON DELETE CASCADE,
                       name VARCHAR(255) NOT NULL,
                       location VARCHAR(255),
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE temperature (
                             id SERIAL PRIMARY KEY,
                             hive_id INTEGER REFERENCES hives(id) ON DELETE CASCADE,
                             value FLOAT NOT NULL,
                             recorded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE weight (
                        id SERIAL PRIMARY KEY,
                        hive_id INTEGER REFERENCES hives(id) ON DELETE CASCADE,
                        value FLOAT NOT NULL,
                        recorded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE noise (
                       id SERIAL PRIMARY KEY,
                       hive_id INTEGER REFERENCES hives(id) ON DELETE CASCADE,
                       value FLOAT NOT NULL,
                       recorded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE tasks (
                       id SERIAL PRIMARY KEY,
                       hive_id INTEGER REFERENCES hives(id) ON DELETE CASCADE,
                       title VARCHAR(255) NOT NULL,
                       description TEXT,
                       status VARCHAR(50) DEFAULT 'pending',
                       due_date TIMESTAMP,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
