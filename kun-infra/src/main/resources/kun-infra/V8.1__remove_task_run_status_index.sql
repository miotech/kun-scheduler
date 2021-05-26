-- kun_wf_task_run.status is a column with duplicated, enumerate values.
-- Adding BTREE index on it will not help.
DROP INDEX IF EXISTS kun_wf_task_run_status_index;
