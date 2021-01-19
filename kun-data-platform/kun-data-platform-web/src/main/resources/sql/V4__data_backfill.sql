CREATE TABLE kun_dp_backfill (
    id BIGINT NOT NULL,
    name VARCHAR(512) NOT NULL,
    creator BIGINT NOT NULL,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

CREATE TABLE kun_dp_backfill_task_run_relation (
    backfill_id BIGINT NOT NULL,
    task_run_id BIGINT NOT NULL,
    task_id BIGINT NOT NULL,
    task_definition_id BIGINT NOT NULL,
    PRIMARY KEY (backfill_id, task_run_id)
);

CREATE INDEX IF NOT EXISTS kun_dp_backfill_idx_name on kun_dp_backfill (name);

CREATE INDEX IF NOT EXISTS kun_dp_backfill__idx_creator on kun_dp_backfill (creator);

CREATE INDEX IF NOT EXISTS kun_dp_backfill_task_run_relation__idx_backfill_id on kun_dp_backfill_task_run_relation (backfill_id);
