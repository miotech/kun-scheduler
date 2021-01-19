CREATE TABLE kun_dp_data_backfill_group (
    id BIGINT NOT NULL,
    name VARCHAR(512) NOT NULL,
    creator BIGINT NOT NULL,
    commit_msg TEXT,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

CREATE TABLE kun_dp_data_backfill_group_tasks (
    backfill_id BIGINT NOT NULL,
    task_run_id BIGINT NOT NULL,
    task_id BIGINT NOT NULL,
    task_definition_id BIGINT NOT NULL,
    task_definition_name VARCHAR(512) NOT NULL,
    PRIMARY KEY (backfill_id, task_run_id)
);

CREATE INDEX IF NOT EXISTS kun_dp_data_backfill_group__idx_name on kun_dp_data_backfill_group (name);

CREATE INDEX IF NOT EXISTS kun_dp_data_backfill_group__idx_creator on kun_dp_data_backfill_group (creator);

CREATE INDEX IF NOT EXISTS kun_dp_data_backfill_group_tasks__idx_backfill_id on kun_dp_data_backfill_group_tasks (backfill_id);
