CREATE TABLE chat_messages (
    id              BIGSERIAL PRIMARY KEY,
    room_id         BIGINT       REFERENCES chat_rooms(id) ON DELETE CASCADE,
    sender_id       BIGINT       NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    recipient_id    BIGINT       REFERENCES users(id) ON DELETE CASCADE,
    message_type    VARCHAR(20)  NOT NULL DEFAULT 'CHAT',
    content         VARCHAR(2000) NOT NULL,
    deleted         BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- Room messages index
CREATE INDEX idx_chat_messages_room_time
    ON chat_messages (room_id, created_at DESC)
    WHERE deleted = FALSE;

-- Direct message index (both directions)
CREATE INDEX idx_chat_messages_dm
    ON chat_messages (sender_id, recipient_id, created_at DESC)
    WHERE deleted = FALSE;

COMMENT ON TABLE chat_messages IS 'Chat messages: room + direct';
COMMENT ON COLUMN chat_messages.message_type IS
    'CHAT, JOIN, LEAVE, SYSTEM (JOIN/LEAVE are ephemeral, not stored)';
COMMENT ON COLUMN chat_messages.room_id IS
    'NULL for direct messages';
COMMENT ON COLUMN chat_messages.recipient_id IS
    'NULL for room messages';

-- Direct messages are stored with both room_id=NULL and recipient_id set.
-- Room messages have room_id set and recipient_id=NULL.
-- Add a CHECK to enforce this:

ALTER TABLE chat_messages ADD CONSTRAINT chk_chat_target
    CHECK (
        (room_id IS NOT NULL AND recipient_id IS NULL) OR
        (room_id IS NULL AND recipient_id IS NOT NULL)
    );
