-- Migration SQL for Unified Asset Management

-- 1. Update the existing asset table
ALTER TABLE asset RENAME COLUMN meta_file_url TO final_url;
ALTER TABLE asset ALTER COLUMN final_url DROP NOT NULL; -- Allow NULL for async generation
ALTER TABLE asset ADD COLUMN IF NOT EXISTS prompt TEXT;
ALTER TABLE asset ADD COLUMN IF NOT EXISTS raw_url VARCHAR(512);
ALTER TABLE asset ADD COLUMN IF NOT EXISTS resized_url VARCHAR(512);
ALTER TABLE asset ADD COLUMN IF NOT EXISTS status VARCHAR(20) DEFAULT 'COMPLETED';
ALTER TABLE asset ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE asset ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

-- 2. Add asset_id to clue and suspect tables
ALTER TABLE clue ADD COLUMN IF NOT EXISTS asset_id BIGINT;
ALTER TABLE suspect ADD COLUMN IF NOT EXISTS asset_id BIGINT;

-- 3. Migrate existing assets from clue/suspect to asset table
-- For clues
INSERT INTO asset (name, final_url, status)
SELECT 'clue-' || scenario_id || '-' || clue_id, assets_url, 'COMPLETED'
FROM clue
WHERE assets_url IS NOT NULL;

UPDATE clue
SET asset_id = a.id
FROM asset a
WHERE a.name = 'clue-' || clue.scenario_id || '-' || clue.clue_id;

-- For suspects
INSERT INTO asset (name, final_url, status)
SELECT 'suspect-' || scenario_id || '-' || suspect_id, assets_url, 'COMPLETED'
FROM suspect
WHERE assets_url IS NOT NULL;

UPDATE suspect
SET asset_id = a.id
FROM asset a
WHERE a.name = 'suspect-' || suspect.scenario_id || '-' || suspect.suspect_id;

-- 4. Remove old assets_url columns
ALTER TABLE clue DROP COLUMN IF EXISTS assets_url;
ALTER TABLE suspect DROP COLUMN IF EXISTS assets_url;

-- 5. Add foreign key constraints (optional but recommended)
-- ALTER TABLE clue ADD CONSTRAINT fk_clue_asset_id FOREIGN KEY (asset_id) REFERENCES asset(id);
-- ALTER TABLE suspect ADD CONSTRAINT fk_suspect_asset_id FOREIGN KEY (asset_id) REFERENCES asset(id);

ALTER TABLE asset ALTER COLUMN final_url DROP NOT NULL;