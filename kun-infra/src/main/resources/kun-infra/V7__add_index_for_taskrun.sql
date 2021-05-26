CREATE INDEX IF NOT EXISTS kun_wf_task_run_start_at_index
    on kun_wf_task_run (start_at);

CREATE INDEX IF NOT EXISTS kun_wf_task_run_status_index
    on kun_wf_task_run (status);
