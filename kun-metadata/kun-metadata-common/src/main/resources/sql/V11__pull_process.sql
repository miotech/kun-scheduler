CREATE TABLE IF NOT EXISTS kun_mt_pull_process (
    process_id BIGSERIAL PRIMARY KEY,
    process_type VARCHAR(64) NOT NULL,   -- 'DATASOURCE' / 'DATASET'
    datasource_id BIGINT,    -- not null when process_type = 'DATASOURCE'
    dataset_id BIGINT,       -- not null when process_type = 'DATASET'
    mce_task_run_id BIGINT,  -- not null when process_type = 'DATASOURCE' OR 'DATASET'
    mse_task_run_id BIGINT,  -- preserved column
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE INDEX kun_mt_pull_process__datasource__idx ON kun_mt_pull_process (datasource_id);
CREATE INDEX kun_mt_pull_process__dataset_id__idx ON kun_mt_pull_process (dataset_id);
