ALTER TABLE kun_wf_task ADD COLUMN retries int8 DEFAULT 0;
ALTER TABLE kun_wf_task ADD COLUMN retry_delay int8 NULL;
ALTER TABLE kun_wf_task_attempt ADD COLUMN retry_times int8 NOT NULL DEFAULT 0;