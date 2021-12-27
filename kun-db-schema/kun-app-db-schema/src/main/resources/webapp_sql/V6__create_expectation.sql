CREATE TABLE IF NOT EXISTS kun_dq_expectation (
    id BIGSERIAL PRIMARY KEY,
    name varchar(128) NOT NULL,
    types varchar(1024),
    description varchar,
    method jsonb NOT NULL,
    trigger varchar(64) NOT NULL,
    dataset_gid bigint NOT NULL,
    task_id bigint,
    is_blocking bool NOT NULL,
    create_time timestamp NOT NULL,
    update_time timestamp NOT NULL,
    create_user varchar(64) NOT NULL,
    update_user varchar(64) NOT NULL
);

CREATE TABLE IF NOT EXISTS kun_dq_expectation_run (
    id BIGSERIAL PRIMARY KEY,
    expectation_id bigint NOT NULL,
    passed bool NOT NULL,
    execution_result varchar,
    assertion_result jsonb NOT NULL,
    continuous_failing_count bigint NOT NULL,
    update_time timestamp NOT NULL
);