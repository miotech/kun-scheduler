DROP TABLE IF EXISTS kun_wf_operator;

DROP TABLE IF EXISTS kun_wf_task;

DROP TABLE IF EXISTS kun_wf_tick_task_mapping;

DROP TABLE IF EXISTS kun_wf_task_relations;

DROP TABLE IF EXISTS kun_wf_task_run;

DROP TABLE IF EXISTS kun_wf_task_attempt;

DROP TABLE IF EXISTS kun_wf_task_run_relations;

CREATE TABLE kun_wf_operator (
    id BIGINT PRIMARY KEY,
    name VARCHAR(128) UNIQUE,
    description VARCHAR(16384) NOT NULL,
    params JSONB NOT NULL,
    class_name VARCHAR(16384) NOT NULL,
    package VARCHAR(16384) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE kun_wf_task (
    id BIGINT PRIMARY KEY,
    name VARCHAR(1024) NOT NULL,
    description VARCHAR(16384) NOT NULL,
    operator_id BIGINT NOT NULL,
    arguments JSONB NOT NULL,
    variable_defs JSONB NOT NULL,
    schedule JSONB NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE kun_wf_tick_task_mapping (
    scheduled_tick VARCHAR(64) NOT NULL,
    task_id BIGINT NOT NULL,
    PRIMARY KEY (scheduled_tick, task_id)
);

CREATE TABLE kun_wf_task_relations (
    upstream_task_id BIGINT NOT NULL,
    downstream_task_id BIGINT NOT NULL,
    dependency_function VARCHAR(1024) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (upstream_task_id, downstream_task_id)
);

CREATE INDEX IF NOT EXISTS kun_wf_task_relations_downstream_task_id_idx
    ON kun_wf_task_relations (downstream_task_id);

CREATE TABLE kun_wf_task_run (
    id BIGINT PRIMARY KEY,
    task_id BIGINT NOT NULL,
    variables JSONB NOT NULL,
    scheduled_tick VARCHAR(64) NOT NULL,
    status VARCHAR(64) NULL,
    start_at TIMESTAMP NULL,
    end_at TIMESTAMP NULL,
    inlets JSONB NULL,
    outlets JSONB NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE kun_wf_task_attempt (
    id BIGINT PRIMARY KEY,
    task_run_id BIGINT NOT NULL,
    attempt INT NOT NULL,
    status VARCHAR(64) NOT NULL,
    log_path VARCHAR(1024) NULL,
    start_at TIMESTAMP NULL,
    end_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE kun_wf_task_run_relations (
    upstream_task_run_id BIGINT NOT NULL,
    downstream_task_run_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (upstream_task_run_id, downstream_task_run_id)
);

CREATE INDEX IF NOT EXISTS kun_wf_task_run_relations_downstream_task_run_id_idx
    ON kun_wf_task_run_relations (downstream_task_run_id);

CREATE TABLE IF NOT EXISTS kun_mt_dataset_gid (
	data_store jsonb not null,
	dataset_gid bigint not null
);

CREATE TABLE IF NOT EXISTS kun_mt_dataset (
	gid bigint primary key,
	name varchar(1024) not null,
	datasource_id bigint not null,
	schema jsonb,
	data_store jsonb
);

CREATE TABLE IF NOT EXISTS kun_mt_dataset_field (
	id bigserial primary key,
	dataset_gid bigint not null,
	name varchar(1024) not null,
	type varchar(64) not null,
	description varchar(16384),
	raw_type varchar(16384)
);

CREATE TABLE IF NOT EXISTS kun_mt_dataset_stats (
	id bigserial primary key,
	dataset_gid bigint not null,
	stats_date timestamp not null,
	row_count bigint not null,
	updator varchar(1024)
);

CREATE TABLE IF NOT EXISTS kun_mt_dataset_field_stats (
	id bigserial primary key,
	field_id bigint not null,
	stats_date timestamp not null,
	distinct_count bigint not null,
	nonnull_count bigint not null,
	updator varchar(1024)
);

CREATE TABLE IF NOT EXISTS kun_mt_dataset_relations (
	upstream_dataset_gid bigint not null,
	downstream_dataset_gid bigint not null,
	task_id bigint not null
);

