DROP TABLE IF EXISTS kun_wf_worker_instance;

CREATE TABLE kun_wf_worker_instance (
    id BIGINT task_attempt_id,
    VARCHAR(256) worker_id,
    VARCHAR(64) env,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
    PRIMARY KEY (task_attempt, worker_id)
);