CREATE TABLE IF NOT EXISTS hubble_db.stored_files
(
    id              UUID PRIMARY KEY      DEFAULT uuid_generate_v4(),
    original_name   VARCHAR(255) NOT NULL,
    content_type    VARCHAR(100) NOT NULL,
    size_bytes      BIGINT       NOT NULL,
    storage_path    VARCHAR(500) NOT NULL,
    checksum_sha256 CHAR(64)     NOT NULL DEFAULT '',
    created_at      TIMESTAMP(3)          DEFAULT CURRENT_TIMESTAMP(3)
);

ALTER TABLE hubble_db.users
    ADD COLUMN IF NOT EXISTS avatar_file_id UUID;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_users_avatar_file'
    ) THEN
        ALTER TABLE hubble_db.users
            ADD CONSTRAINT fk_users_avatar_file
                FOREIGN KEY (avatar_file_id) REFERENCES hubble_db.stored_files (id);
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_users_avatar_file ON hubble_db.users (avatar_file_id);
