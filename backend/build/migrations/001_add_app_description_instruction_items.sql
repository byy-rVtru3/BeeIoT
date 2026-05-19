CREATE TABLE IF NOT EXISTS app_description (
    id INT PRIMARY KEY DEFAULT 1 CHECK (id = 1),
    title VARCHAR(80) NOT NULL,
    short VARCHAR(160) NOT NULL,
    full TEXT NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_by VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS instruction_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title VARCHAR(100) NOT NULL,
    body TEXT NOT NULL,
    numbered BOOLEAN NOT NULL DEFAULT false,
    position INT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX IF NOT EXISTS instruction_items_position_idx
    ON instruction_items(position);
