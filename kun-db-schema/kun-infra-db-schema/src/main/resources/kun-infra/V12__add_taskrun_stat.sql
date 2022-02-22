CREATE TABLE IF NOT EXISTS kun_wf_task_run_stat (
    "id" BIGINT PRIMARY KEY,
    "average_running_time" INT4 NOT NULL,
    "average_queuing_time" INT4 NOT NULL
)