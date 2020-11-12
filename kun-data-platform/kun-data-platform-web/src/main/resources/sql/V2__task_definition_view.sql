CREATE TABLE kun_dp_task_definition_view (
    id BIGINT NOT NULL,
    name VARCHAR(1024) UNIQUE NOT NULL,
    creator BIGINT NOT NULL,
    last_modifier BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

CREATE TABLE kun_dp_view_task_definition_relation (
    view_id BIGINT NOT NULL,
    task_def_id BIGINT NOT NULL,
    PRIMARY KEY (view_id, task_def_id)
);
