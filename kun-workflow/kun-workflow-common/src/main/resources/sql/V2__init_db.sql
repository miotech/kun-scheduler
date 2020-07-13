DROP TABLE IF EXISTS kun_wf_task_tags;

CREATE TABLE kun_wf_task_tags (
    task_id BIGINT NOT NULL,
    tag_key VARCHAR(256) NOT NULL,
    tag_value VARCHAR(16384) NOT NULL,
    PRIMARY KEY (task_id, tag_key)
);
