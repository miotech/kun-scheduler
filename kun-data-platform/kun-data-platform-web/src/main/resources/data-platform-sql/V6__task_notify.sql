CREATE TABLE kun_dp_task_notify_config (
    id BIGSERIAL PRIMARY KEY,
    workflow_task_id BIGINT UNIQUE NOT NULL,
    notify_when varchar(32) DEFAULT 'SYSTEM_DEFAULT' NOT NULL,
    notify_config JSONB NOT NULL,
    created_at timestamp default CURRENT_TIMESTAMP not null,
    updated_at timestamp default CURRENT_TIMESTAMP not null
);

CREATE INDEX IF NOT EXISTS kun_dp_task_notify_config__notify_when ON kun_dp_task_notify_config (notify_when);
