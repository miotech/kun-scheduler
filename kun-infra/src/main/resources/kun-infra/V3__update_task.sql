ALTER TABLE kun_wf_operator DROP COLUMN params;

ALTER TABLE kun_wf_task RENAME COLUMN variable_defs TO config;

ALTER TABLE kun_wf_task DROP COLUMN arguments;

ALTER TABLE kun_wf_task_run RENAME COLUMN variables TO config;
