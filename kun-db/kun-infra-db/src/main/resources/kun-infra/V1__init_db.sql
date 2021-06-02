
CREATE TABLE IF NOT EXISTS kun_mt_dataset (
    "gid" bigint primary key,
    "name" varchar(1024) NOT NULL,
    "datasource_id" int8 NOT NULL,
    "schema" jsonb,
    "data_store" jsonb,
    "database_name" varchar(1024),
    "dsi" varchar(1024),
    "deleted" bool DEFAULT false
);


-- Table Definition
CREATE TABLE IF NOT EXISTS kun_mt_dataset_field (
    id bigserial primary key,
    "dataset_gid" int8 NOT NULL,
    "name" varchar(1024) NOT NULL,
    "type" varchar(64) NOT NULL,
    "description" varchar(16384),
    "raw_type" varchar(16384),
    "is_primary_key" bool DEFAULT false,
    "is_nullable" bool DEFAULT true
);


CREATE TABLE IF NOT EXISTS kun_mt_dataset_field_mapping (
    "datasource_type" varchar(64),
    "pattern" varchar(128),
    "type" varchar(64)
);


-- Table Definition
CREATE TABLE IF NOT EXISTS kun_mt_dataset_field_stats (
    id bigserial primary key,
    "field_id" int8 NOT NULL,
    "stats_date" timestamp NOT NULL,
    "distinct_count" int8 NOT NULL,
    "nonnull_count" int8 NOT NULL,
    "updator" varchar(1024)
);

-- This script only contains the table creation statements and does not fully represent the table in the database. It's still missing: indices, triggers. Do not use it as a backup.

-- Table Definition
CREATE TABLE IF NOT EXISTS kun_mt_dataset_gid (
    "data_store" jsonb NOT NULL,
    "dataset_gid" int8 NOT NULL,
    "dsi" varchar(1024)
);


-- This script only contains the table creation statements and does not fully represent the table in the database. It's still missing: indices, triggers. Do not use it as a backup.

-- Table Definition
CREATE TABLE IF NOT EXISTS kun_mt_dataset_lifecycle (
    "dataset_gid" int8,
    "changed" jsonb,
    "fields" jsonb,
    "status" varchar(64),
    "create_at" timestamp
);


-- This script only contains the table creation statements and does not fully represent the table in the database. It's still missing: indices, triggers. Do not use it as a backup.

-- Table Definition
CREATE TABLE IF NOT EXISTS kun_mt_dataset_snapshot (
    id bigint PRIMARY KEY,
    "dataset_gid" int8 NOT NULL,
    "schema_snapshot" jsonb,
    "statistics_snapshot" jsonb,
    "schema_at" timestamp,
    "statistics_at" timestamp
);


-- Table Definition
CREATE TABLE IF NOT EXISTS kun_mt_dataset_stats (
    id bigserial primary key,
    "dataset_gid" int8 NOT NULL,
    "stats_date" timestamp NOT NULL,
    "row_count" int8 NOT NULL,
    "updator" varchar(1024),
    "last_updated_time" timestamp,
    "total_byte_size" int8
);

CREATE TABLE IF NOT EXISTS kun_mt_pull_process (
    process_id BIGSERIAL PRIMARY KEY,
    process_type VARCHAR(64) NOT NULL,   -- 'DATASOURCE' / 'DATASET'
    datasource_id BIGINT,    -- not null when process_type = 'DATASOURCE'
    dataset_id BIGINT,       -- not null when process_type = 'DATASET'
    mce_task_run_id BIGINT,  -- not null when process_type = 'DATASOURCE' OR 'DATASET'
    mse_task_run_id BIGINT,  -- preserved column
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);


-- Table Definition
CREATE TABLE IF NOT EXISTS kun_wf_checkpoint (
    id BIGSERIAL PRIMARY KEY,
    "checkpoint_tick" varchar(64) NOT NULL,
    "created_at" timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- This script only contains the table creation statements and does not fully represent the table in the database. It's still missing: indices, triggers. Do not use it as a backup.

-- Table Definition
CREATE TABLE IF NOT EXISTS kun_wf_operator (
    id BIGINT PRIMARY KEY,
    "name" varchar(128),
    "description" varchar(16384) NOT NULL,
    "class_name" varchar(16384) NOT NULL,
    "package" varchar(16384) NOT NULL,
    "created_at" timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updated_at" timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- This script only contains the table creation statements and does not fully represent the table in the database. It's still missing: indices, triggers. Do not use it as a backup.

-- Table Definition
CREATE TABLE IF NOT EXISTS kun_wf_task (
    id BIGINT PRIMARY KEY,
    "name" varchar(1024) NOT NULL,
    "description" varchar(16384) NOT NULL,
    "operator_id" int8 NOT NULL,
    "config" jsonb NOT NULL,
    "schedule" jsonb NOT NULL,
    "created_at" timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updated_at" timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "queue_name" varchar(64) DEFAULT 'default'::character varying,
    "priority" int4
);

-- This script only contains the table creation statements and does not fully represent the table in the database. It's still missing: indices, triggers. Do not use it as a backup.

-- Table Definition
CREATE TABLE IF NOT EXISTS kun_wf_task_attempt (
    id BIGINT PRIMARY KEY,
    "task_run_id" int8 NOT NULL,
    "attempt" int4 NOT NULL,
    "status" varchar(64) NOT NULL,
    "log_path" varchar(1024),
    "start_at" timestamp,
    "end_at" timestamp,
    "created_at" timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updated_at" timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "queue_name" varchar(64) DEFAULT 'default'::character varying,
    "priority" int4
);



-- This script only contains the table creation statements and does not fully represent the table in the database. It's still missing: indices, triggers. Do not use it as a backup.

-- Table Definition
CREATE TABLE IF NOT EXISTS kun_wf_task_relations (
    "upstream_task_id" int8 NOT NULL,
    "downstream_task_id" int8 NOT NULL,
    "dependency_function" varchar(1024) NOT NULL,
    "created_at" timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updated_at" timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "dependency_level" varchar(64) NOT NULL DEFAULT 'STRONG'::character varying,
    PRIMARY KEY ("upstream_task_id","downstream_task_id")
);

-- This script only contains the table creation statements and does not fully represent the table in the database. It's still missing: indices, triggers. Do not use it as a backup.

-- Table Definition
CREATE TABLE IF NOT EXISTS kun_wf_task_run (
    id BIGINT PRIMARY KEY,
    "task_id" int8 NOT NULL,
    "config" jsonb NOT NULL,
    "scheduled_tick" varchar(64) NOT NULL,
    "status" varchar(64),
    "start_at" timestamp,
    "end_at" timestamp,
    "inlets" jsonb,
    "outlets" jsonb,
    "created_at" timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updated_at" timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "schedule_type" varchar(64) NOT NULL DEFAULT 'NONE'::character varying,
    "queue_name" varchar(64) DEFAULT 'default'::character varying,
    "priority" int4
);

-- This script only contains the table creation statements and does not fully represent the table in the database. It's still missing: indices, triggers. Do not use it as a backup.

-- Table Definition
CREATE TABLE IF NOT EXISTS kun_wf_task_run_relations (
    "upstream_task_run_id" int8 NOT NULL,
    "downstream_task_run_id" int8 NOT NULL,
    "created_at" timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updated_at" timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "dependency_level" varchar(64) NOT NULL DEFAULT 'STRONG'::character varying,
    "dependency_status" varchar(64) NOT NULL DEFAULT '''CREATED''::character varying'::character varying,
    PRIMARY KEY ("upstream_task_run_id","downstream_task_run_id")
);

-- This script only contains the table creation statements and does not fully represent the table in the database. It's still missing: indices, triggers. Do not use it as a backup.

-- Table Definition
CREATE TABLE IF NOT EXISTS kun_wf_task_tags (
    "task_id" BIGINT NOT NULL,
    "tag_key" varchar(256) NOT NULL,
    "tag_value" varchar(16384) NOT NULL,
    PRIMARY KEY ("task_id","tag_key")
);


CREATE TABLE IF NOT EXISTS kun_wf_tick_task_mapping (
    "scheduled_tick" varchar(64) NOT NULL,
    "task_id" int8 NOT NULL,
    PRIMARY KEY ("scheduled_tick","task_id")
);

CREATE TABLE IF NOT EXISTS kun_wf_variable (
    "key" varchar(256) NOT NULL,
    "value" text NOT NULL,
    "is_encrypted" bool NOT NULL,
    PRIMARY KEY ("key")
);

CREATE INDEX IF NOT EXISTS index_snapshot_dataset_gid_schema_at ON kun_mt_dataset_snapshot (dataset_gid, schema_at);

CREATE INDEX IF NOT EXISTS index_snapshot_dataset_gid_statistics_at ON kun_mt_dataset_snapshot (dataset_gid, statistics_at);

CREATE UNIQUE INDEX IF NOT EXISTS kun_mt_dataset_snapshot_pkey ON kun_mt_dataset_snapshot (id);

CREATE UNIQUE INDEX IF NOT EXISTS kun_mt_dataset_bak_pkey ON kun_mt_dataset (gid);

CREATE INDEX IF NOT EXISTS kun_mt_dataset_field_gid ON kun_mt_dataset_field (dataset_gid);

CREATE UNIQUE INDEX IF NOT EXISTS kun_mt_dataset_field_pkey ON kun_mt_dataset_field (id);

CREATE INDEX IF NOT EXISTS kun_mt_dataset_field_stats_field_id_index ON kun_mt_dataset_field_stats (field_id);

CREATE UNIQUE INDEX IF NOT EXISTS kun_mt_dataset_field_stats_pkey ON kun_mt_dataset_field_stats (id);

CREATE INDEX IF NOT EXISTS kun_mt_dataset_stat_gid ON kun_mt_dataset_stats (dataset_gid);

CREATE UNIQUE INDEX IF NOT EXISTS kun_mt_dataset_stats_pkey ON kun_mt_dataset_stats (id);

CREATE UNIQUE INDEX IF NOT EXISTS kun_wf_checkpoint_pkey ON kun_wf_checkpoint (id);

CREATE UNIQUE INDEX IF NOT EXISTS kun_wf_operator_name_key ON kun_wf_operator (name);

CREATE UNIQUE INDEX IF NOT EXISTS kun_wf_operator_pkey ON kun_wf_operator (id);

CREATE UNIQUE INDEX IF NOT EXISTS kun_wf_task_attempt_pkey ON kun_wf_task_attempt (id);

CREATE UNIQUE INDEX IF NOT EXISTS kun_wf_task_pkey ON kun_wf_task (id);

CREATE INDEX IF NOT EXISTS kun_wf_task_relations_downstream_task_id_idx ON kun_wf_task_relations (downstream_task_id);

CREATE UNIQUE INDEX IF NOT EXISTS kun_wf_task_relations_pkey ON kun_wf_task_relations (upstream_task_id, downstream_task_id);

CREATE INDEX IF NOT EXISTS kun_wf_task_run_created_at_index ON kun_wf_task_run (created_at);

CREATE UNIQUE INDEX IF NOT EXISTS kun_wf_task_run_pkey ON kun_wf_task_run (id);

CREATE INDEX IF NOT EXISTS kun_wf_task_run_start_at_index ON kun_wf_task_run (start_at);

CREATE INDEX IF NOT EXISTS kun_wf_task_run_task_id_index ON kun_wf_task_run (task_id);

CREATE INDEX IF NOT EXISTS kun_wf_task_run_relations_downstream_task_run_id_idx ON kun_wf_task_run_relations (downstream_task_run_id);

CREATE UNIQUE INDEX IF NOT EXISTS kun_wf_task_run_relations_pkey ON kun_wf_task_run_relations (upstream_task_run_id, downstream_task_run_id);

CREATE UNIQUE INDEX IF NOT EXISTS kun_wf_task_tags_pkey ON kun_wf_task_tags (task_id, tag_key);

CREATE UNIQUE INDEX IF NOT EXISTS kun_wf_tick_task_mapping_pkey ON kun_wf_tick_task_mapping (scheduled_tick, task_id);

CREATE UNIQUE INDEX IF NOT EXISTS kun_wf_variable_key_uidx ON kun_wf_variable (key);

CREATE UNIQUE INDEX IF NOT EXISTS kun_wf_variable_pkey ON kun_wf_variable (key);

CREATE INDEX kun_mt_pull_process__datasource__idx ON kun_mt_pull_process (datasource_id);

CREATE INDEX kun_mt_pull_process__dataset_id__idx ON kun_mt_pull_process (dataset_id);

