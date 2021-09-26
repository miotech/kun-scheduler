CREATE TABLE IF NOT EXISTS kun_dp_task_timeline (
    id bigserial PRIMARY KEY,
    task_run_id int8 NOT NULL,
    definition_id int8 NOT NULL,
    level int4 NULL,
    deadline varchar(64) NULL,
    root_definition_id int8 NULL,
    created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP
);