-- Migration: add tasks table for hive work items.
-- Run (in postgres container):
--   docker compose -f build/docker-compose.yml exec db psql -U ${DB_USER} -d ${DB_NAME} -f /docker-entrypoint-initdb.d/migrate_add_tasks.sql
-- Or locally:
--   psql -h <host> -U <user> -d <db> -f build/migrate_add_tasks.sql

CREATE TABLE IF NOT EXISTS tasks (
                                     id TEXT PRIMARY KEY,
                                     email TEXT NOT NULL,
                                     hive_name TEXT NOT NULL,
                                     title TEXT NOT NULL,
                                     description TEXT,
                                     created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS tasks_email_idx ON tasks (email);
CREATE INDEX IF NOT EXISTS tasks_email_hive_idx ON tasks (email, hive_name);
