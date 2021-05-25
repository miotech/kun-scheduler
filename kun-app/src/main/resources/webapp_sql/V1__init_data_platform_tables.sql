CREATE TABLE IF NOT EXISTS kun_dp_task_definition (
    id BIGINT NOT NULL,
    definition_id BIGINT NOT NULL,
    name VARCHAR(1024) NOT NULL,
    task_template_name VARCHAR(1024) NOT NULL,
    task_payload JSONB,
    creator BIGINT NOT NULL,
    owner BIGINT NOT NULL,
    is_archived BOOLEAN NOT NULL,
    last_modifier BIGINT NOT NULL,
    create_time TIMESTAMP NOT NULL DEFAULT  CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT  CURRENT_TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT  CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS kun_dp_task_datasets (
    id BIGINT NOT NULL,
    definition_id BIGINT NOT NULL,
    datastore_id BIGINT NOT NULL,
    dataset_name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT  CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT  CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS kun_dp_data_store (
    id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    url VARCHAR(255) NOT NULL,
    username VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    type VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT  CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT  CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS kun_dp_task_try (
    id BIGINT NOT NULL,
    definition_id BIGINT NOT NULL,
    wf_task_id BIGINT NOT NULL,
    wf_task_run_id BIGINT NOT NULL,
    task_config JSONB,
    creator BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT  CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT  CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS kun_dp_deployed_task (
    id BIGINT NOT NULL,
    definition_id BIGINT NOT NULL,
    name VARCHAR(1024) NOT NULL,
    task_template_name VARCHAR(1024) NOT NULL,
    wf_task_id BIGINT NOT NULL,
    owner BIGINT NOT NULL,
    commit_id BIGINT NOT NULL,
    is_archived BOOLEAN NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT  CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT  CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

CREATE UNIQUE INDEX IF NOT EXISTS kun_dp_deployed__task_wf_task_id__unique ON kun_dp_deployed_task (wf_task_id);

CREATE TABLE IF NOT EXISTS kun_dp_task_commit (
    id BIGINT NOT NULL,
    task_def_id BIGINT NOT NULL,
    version	 INT NOT NULL,
    message TEXT,
    snapshot JSONB,
    committer BIGINT NOT NULL ,
    committed_at TIMESTAMP NOT NULL,
    commit_type VARCHAR(255) NOT NULL,
    commit_status VARCHAR(255) NOT NULL,
    is_latest BOOLEAN DEFAULT  FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT  CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT  CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS kun_dp_deploy (
    id BIGINT NOT NULL,
    name VARCHAR(1024) NOT NULL,
    creator BIGINT NOT NULL ,
    submitted_at TIMESTAMP NOT NULL,
    deployer BIGINT NULL ,
    deployed_at TIMESTAMP NULL,
    status VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT  CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT  CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS kun_dp_deploy_commits (
    deploy_id BIGINT NOT NULL,
    commit_id BIGINT NOT NULL,
    deploy_status VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT  CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT  CURRENT_TIMESTAMP,
    PRIMARY KEY (deploy_id, commit_id)
);

CREATE TABLE IF NOT EXISTS kun_dp_task_template (
    name VARCHAR(255) NOT NULL,
    template_type VARCHAR(255) NOT NULL,
    template_group VARCHAR(255) NOT NULL,
    operator_id BIGINT NOT NULL,
    default_values JSONB,
    display_parameters JSONB,
    renderer_class VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT  CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT  CURRENT_TIMESTAMP,
    PRIMARY KEY (name)
);

CREATE TABLE IF NOT EXISTS kun_dp_task_definition_view (
    id BIGINT NOT NULL,
    name VARCHAR(1024) UNIQUE NOT NULL,
    creator BIGINT NOT NULL,
    last_modifier BIGINT,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS kun_dp_view_task_definition_relation (
    view_id BIGINT NOT NULL,
    task_def_id BIGINT NOT NULL,
    PRIMARY KEY (view_id, task_def_id)
);

CREATE TABLE IF NOT EXISTS kun_dp_task_relation
(
    upstream_task_id bigint not null,
    downstream_task_id bigint not null,
    created_at timestamp default CURRENT_TIMESTAMP not null,
    updated_at timestamp default CURRENT_TIMESTAMP not null,
    constraint kun_dp_task_relations_pkey
        primary key (upstream_task_id, downstream_task_id)
);

CREATE TABLE IF NOT EXISTS kun_dp_backfill (
    id BIGINT NOT NULL,
    name VARCHAR(512) NOT NULL,
    creator BIGINT NOT NULL,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS kun_dp_backfill_task_run_relation (
    backfill_id BIGINT NOT NULL,
    task_run_id BIGINT NOT NULL,
    task_id BIGINT NOT NULL,
    task_definition_id BIGINT NOT NULL,
    PRIMARY KEY (backfill_id, task_run_id)
);

CREATE INDEX IF NOT EXISTS kun_dp_backfill_idx_name on kun_dp_backfill (name);

CREATE INDEX IF NOT EXISTS kun_dp_backfill__idx_creator on kun_dp_backfill (creator);

CREATE INDEX IF NOT EXISTS kun_dp_backfill__idx_create_time on kun_dp_backfill (create_time);

CREATE INDEX IF NOT EXISTS kun_dp_backfill_task_run_relation__idx_backfill_id on kun_dp_backfill_task_run_relation (backfill_id);

CREATE TABLE IF NOT EXISTS kun_dp_task_notify_config (
                                           id BIGSERIAL PRIMARY KEY,
                                           workflow_task_id BIGINT UNIQUE NOT NULL,
                                           notify_when varchar(32) DEFAULT 'SYSTEM_DEFAULT' NOT NULL,
                                           notify_config JSONB NOT NULL,
                                           created_at timestamp default CURRENT_TIMESTAMP not null,
                                           updated_at timestamp default CURRENT_TIMESTAMP not null
);

CREATE INDEX IF NOT EXISTS kun_dp_task_notify_config__notify_when ON kun_dp_task_notify_config (notify_when);
