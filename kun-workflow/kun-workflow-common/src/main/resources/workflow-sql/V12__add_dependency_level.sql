ALTER TABLE kun_wf_task_relations ADD COLUMN dependency_level VARCHAR(64) NOT NULL DEFAULT 'STRONG';
ALTER TABLE kun_wf_task_run_relations ADD COLUMN dependency_level VARCHAR(64) NOT NULL DEFAULT 'STRONG';
ALTER TABLE kun_wf_task_run_relations ADD COLUMN dependency_status VARCHAR(64) NOT NULL DEFAULT 'CREATE';



