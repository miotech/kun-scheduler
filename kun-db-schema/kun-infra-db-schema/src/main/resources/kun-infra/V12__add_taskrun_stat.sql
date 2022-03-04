CREATE TABLE IF NOT EXISTS kun_wf_task_run_stat (
    id bigserial PRIMARY KEY,
    "task_run_id" INT8 NOT NULL,
    "average_running_time" INT4 NOT NULL,
    "average_queuing_time" INT4 NOT NULL,
    "created_at" timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updated_at" timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS kun_wf_task_run_stat_task_run_id_index ON kun_wf_task_run_stat (task_run_id);