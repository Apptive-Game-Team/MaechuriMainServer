CREATE TABLE IF NOT EXISTS tag (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(31) UNIQUE NOT NULL
);

CREATE TABLE IF NOT EXISTS asset (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(31) UNIQUE NOT NULL,
    prompt TEXT,
    raw_url VARCHAR(512),
    resized_url VARCHAR(512),
    final_url VARCHAR(512), -- Made nullable for async generation
    status VARCHAR(20) DEFAULT 'COMPLETED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_asset_name ON asset(name);

CREATE TABLE IF NOT EXISTS asset_tag (
    id BIGSERIAL PRIMARY KEY,
    tag_id BIGINT REFERENCES tag(id),
    asset_id BIGINT REFERENCES asset(id)
);

ALTER TABLE asset_tag DROP CONSTRAINT IF EXISTS asset_tag_asset_id_fkey;
ALTER TABLE asset_tag DROP CONSTRAINT IF EXISTS asset_tag_tag_id_fkey;

ALTER TABLE asset_tag
    ADD CONSTRAINT fk_asset_tag_tag_id
        FOREIGN KEY (tag_id) REFERENCES tag(id) ON DELETE CASCADE;
ALTER TABLE asset_tag
    ADD CONSTRAINT fk_asset_tag_asset_id
        FOREIGN KEY (asset_id) REFERENCES asset(id) ON DELETE CASCADE;

CREATE TABLE IF NOT EXISTS game_session_record (
    id BIGSERIAL PRIMARY KEY,
    game_session_id VARCHAR(255) NOT NULL,
    scenario_id BIGINT NOT NULL,
    record_tag VARCHAR(10) NOT NULL,
    record_id BIGINT NOT NULL,
    interacted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(game_session_id, scenario_id, record_tag, record_id)
);

CREATE INDEX IF NOT EXISTS idx_game_session_record_session_id ON game_session_record(game_session_id);
CREATE INDEX IF NOT EXISTS idx_game_session_record_session_scenario ON game_session_record(game_session_id, scenario_id);

CREATE TABLE IF NOT EXISTS scenario (
    scenario_id BIGSERIAL PRIMARY KEY,
    difficulty VARCHAR(10) NOT NULL,
    theme VARCHAR(255) NOT NULL,
    tone VARCHAR(255) NOT NULL,
    language VARCHAR(10) NOT NULL,
    incident_type VARCHAR(255) NOT NULL,
    incident_summary TEXT NOT NULL,
    incident_time_start TIME NOT NULL,
    incident_time_end TIME NOT NULL,
    primary_object VARCHAR(255) NOT NULL,
    crime_time_start TIME NOT NULL,
    crime_time_end TIME NOT NULL,
    crime_method TEXT NOT NULL,
    no_supernatural BOOLEAN NOT NULL,
    no_time_travel BOOLEAN NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    incident_location_id BIGINT,
    crime_location_id BIGINT,
    date DATE
);

CREATE TABLE IF NOT EXISTS location (
    scenario_id BIGINT REFERENCES scenario(scenario_id),
    location_id BIGINT,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL,
    x SMALLINT NOT NULL,
    y SMALLINT NOT NULL,
    width SMALLINT NOT NULL,
    height SMALLINT NOT NULL,
    can_see TEXT NOT NULL, -- jsonb format
    cannot_see TEXT NOT NULL, -- jsonb format
    access_requires TEXT, -- jsonb format
    floor_url VARCHAR(512),
    wall_url VARCHAR(512),
    PRIMARY KEY (scenario_id, location_id)
);

CREATE TABLE IF NOT EXISTS suspect (
    scenario_id BIGINT REFERENCES scenario(scenario_id),
    suspect_id BIGINT,
    name VARCHAR(255) NOT NULL,
    role VARCHAR(255) NOT NULL,
    age INT NOT NULL,
    gender VARCHAR(50) NOT NULL,
    description TEXT NOT NULL,
    is_culprit BOOLEAN NOT NULL,
    motive TEXT,
    alibi_summary TEXT NOT NULL,
    speech_style TEXT NOT NULL,
    emotional_tendency TEXT NOT NULL,
    lying_pattern TEXT NOT NULL,
    critical_clue_ids TEXT NOT NULL, -- jsonb format
    x SMALLINT,
    y SMALLINT,
    visual_description TEXT,
    asset_id BIGINT REFERENCES asset(id),
    PRIMARY KEY (scenario_id, suspect_id)
);

CREATE TABLE IF NOT EXISTS clue (
    scenario_id BIGINT REFERENCES scenario(scenario_id),
    clue_id BIGINT,
    name VARCHAR(255) NOT NULL,
    location_id BIGINT,
    description TEXT NOT NULL,
    logic_explanation TEXT NOT NULL,
    decoded_answer TEXT,
    is_red_herring BOOLEAN NOT NULL,
    related_fact_ids TEXT NOT NULL, -- jsonb format
    x SMALLINT,
    y SMALLINT,
    visual_description TEXT,
    asset_id BIGINT REFERENCES asset(id),
    PRIMARY KEY (scenario_id, clue_id)
);

CREATE TABLE IF NOT EXISTS fact (
    scenario_id BIGINT REFERENCES scenario(scenario_id),
    suspect_id BIGINT,
    fact_id BIGINT,
    threshold INT NOT NULL,
    type VARCHAR(50) NOT NULL,
    content TEXT NOT NULL, -- jsonb format
    embedding bytea,
    PRIMARY KEY (scenario_id, suspect_id, fact_id)
);

CREATE TABLE IF NOT EXISTS scenario_context (
    scenario_id BIGINT REFERENCES scenario(scenario_id),
    context_id BIGINT,
    type VARCHAR(50) NOT NULL,
    content TEXT NOT NULL,
    extra_data TEXT NOT NULL, -- jsonb format
    embedding bytea,
    PRIMARY KEY (scenario_id, context_id)
);
