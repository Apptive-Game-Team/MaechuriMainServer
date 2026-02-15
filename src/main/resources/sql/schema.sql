CREATE TABLE IF NOT EXISTS tag (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(31) UNIQUE NOT NULL
);

CREATE TABLE IF NOT EXISTS asset (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(31) UNIQUE NOT NULL,
    meta_file_url VARCHAR(255) NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_asset_name ON asset(name);

CREATE TABLE IF NOT EXISTS asset_tag (
    id BIGSERIAL PRIMARY KEY,
    tag_id BIGINT REFERENCES tag(id),
    asset_id BIGINT REFERENCES asset(id)
);

ALTER TABLE asset_tag DROP CONSTRAINT asset_tag_asset_id_fkey;
ALTER TABLE asset_tag DROP CONSTRAINT asset_tag_tag_id_fkey;

ALTER TABLE asset_tag
    ADD CONSTRAINT fk_asset_tag_tag_id
        FOREIGN KEY (tag_id) REFERENCES tag(id) ON DELETE CASCADE;
ALTER TABLE asset_tag
    ADD CONSTRAINT fk_asset_tag_asset_id
        FOREIGN KEY (asset_id) REFERENCES asset(id) ON DELETE CASCADE;

CREATE TABLE IF NOT EXISTS game_session_record (
    id BIGSERIAL PRIMARY KEY,
    game_session_id VARCHAR(255) NOT NULL,
    record_tag VARCHAR(10) NOT NULL,
    record_id BIGINT NOT NULL,
    interacted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(game_session_id, record_tag, record_id)
);

CREATE INDEX IF NOT EXISTS idx_game_session_record_session_id ON game_session_record(game_session_id);
