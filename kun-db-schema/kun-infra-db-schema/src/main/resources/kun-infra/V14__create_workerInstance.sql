DROP TABLE IF EXISTS kun_wf_worker_instance;

CREATE TABLE kun_wf_worker_instance (
    task_attempt_id BIGINT  NOT NULL,
    worker_id VARCHAR(256)  NOT NULL,
    env VARCHAR(64) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (task_attempt_id, worker_id)
);
CREATE UNIQUE INDEX idx_worker_instance_UNQ ON kun_wf_worker_instance(task_attempt_id,env)

