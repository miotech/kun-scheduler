ALTER TABLE kun_wf_task ADD queue_name VARCHAR(64)  DEFAULT 'default';
ALTER TABLE kun_wf_task_run ADD queue_name VARCHAR(64)  DEFAULT 'default';
ALTER TABLE kun_wf_task_attempt ADD queue_name VARCHAR(64)  DEFAULT 'default';