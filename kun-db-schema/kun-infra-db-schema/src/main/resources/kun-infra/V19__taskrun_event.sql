ALTER TABLE kun_wf_task_attempt ADD COLUMN task_run_phase int4 default 1;
ALTER TABLE kun_wf_task_run ADD COLUMN task_run_phase int4 default 1;


CREATE TABLE IF NOT EXISTS kun_wf_task_run_transit_event (
    id BIGINT PRIMARY KEY,
    "task_attempt_id" int8 NOT NULL,
    "event_type" varchar(64) ,
    "completed" bool NOT NULL,
    "created_at" timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updated_at" timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP
);