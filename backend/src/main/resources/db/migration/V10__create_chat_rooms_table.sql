CREATE TABLE chat_rooms (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(50)  NOT NULL UNIQUE,
    description     VARCHAR(200),
    created_by      BIGINT       NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    is_default      BOOLEAN      NOT NULL DEFAULT FALSE,
    archived        BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_chat_rooms_name       ON chat_rooms (LOWER(name));
CREATE INDEX idx_chat_rooms_archived   ON chat_rooms (archived);

CREATE TRIGGER chat_rooms_updated_at
    BEFORE UPDATE ON chat_rooms
    FOR EACH ROW
    EXECUTE FUNCTION set_updated_at();

-- Seed default rooms (owner = admin user id 1)
INSERT INTO chat_rooms (name, description, created_by, is_default)
VALUES
    ('general',    'General discussion',          1, TRUE),
    ('help',       'Ask for help',                1, TRUE),
    ('showcase',   'Share your best snippets',    1, TRUE),
    ('random',     'Off-topic chat',              1, TRUE)
ON CONFLICT (name) DO NOTHING;

COMMENT ON TABLE chat_rooms IS 'Chat rooms; default rooms seeded here';
COMMENT ON COLUMN chat_rooms.is_default IS
    'Default rooms cannot be deleted by non-admins';
