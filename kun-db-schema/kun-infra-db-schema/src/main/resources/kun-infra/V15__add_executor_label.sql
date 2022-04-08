ALTER TABLE kun_wf_task ADD COLUMN executor_label varchar(64);
ALTER TABLE kun_wf_task_run ADD COLUMN executor_label varchar(64);
ALTER TABLE kun_wf_task_attempt ADD COLUMN executor_label varchar(64);