CREATE TABLE IF NOT EXISTS kun_wf_target (
    id bigserial primary key,
    "name" varchar(64) NOT NULL,
    "create_at" timestamp,
    "update_at" timestamp

);

CREATE UNIQUE INDEX IF NOT EXISTS kun_wf_target_name ON kun_wf_target (name);

ALTER TABLE kun_wf_task_run ADD COLUMN target jsonb ;

INSERT INTO kun_wf_target (name,create_at,update_at) VALUES ('prod',now(),now());
INSERT INTO kun_wf_target (name,create_at,update_at) VALUES ('dev',now(),now());
