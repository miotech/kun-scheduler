CREATE TABLE IF NOT EXISTS kun_wf_task_run_conditions (
    id bigserial primary key,
    "task_run_id" int8 NOT NULL,
    "condition" jsonb NOT NULL,
    "result" boolean NOT NULL DEFAULT FALSE,
    "type" varchar(64) NOT NULL,
    "created_at" timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updated_at" timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP
);