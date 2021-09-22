CREATE TABLE IF NOT EXISTS kun_dq_case_associated_task_run (
    "id" serial,
    "case_run_id" int8 ,
    "task_run_id" int8,
    "status" varchar(64)
);

ALTER TABLE kun_dq_case ADD COLUMN is_block boolean DEFAULT FALSE ;