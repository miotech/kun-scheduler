ALTER TABLE kun_dp_backfill ALTER COLUMN creator TYPE VARCHAR(1024);

ALTER TABLE kun_dp_deploy ALTER COLUMN creator TYPE VARCHAR(1024);
ALTER TABLE kun_dp_deploy ALTER COLUMN deployer TYPE VARCHAR(1024);

ALTER TABLE kun_dp_deployed_task ALTER COLUMN owner TYPE VARCHAR(1024);


ALTER TABLE kun_dp_task_definition ALTER COLUMN creator TYPE VARCHAR(1024);
ALTER TABLE kun_dp_task_definition ALTER COLUMN owner TYPE VARCHAR(1024);
ALTER TABLE kun_dp_task_definition ALTER COLUMN last_modifier TYPE VARCHAR(1024);

ALTER TABLE kun_dp_task_commit ALTER COLUMN committer TYPE VARCHAR(1024);

ALTER TABLE kun_dp_task_definition_view ALTER COLUMN creator TYPE VARCHAR(1024);
ALTER TABLE kun_dp_task_definition_view ALTER COLUMN last_modifier TYPE VARCHAR(1024);

ALTER TABLE kun_dp_task_try ALTER COLUMN creator TYPE VARCHAR(1024);
