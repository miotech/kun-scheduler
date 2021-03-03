ALTER TABLE  kun_wf_task_run_relations DROP COLUMN dependency_status

ALTER TABLE kun_wf_task_run_relations ADD COLUMN dependency_status VARCHAR(64) NOT NULL DEFAULT 'CREATED';
