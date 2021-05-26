CREATE TABLE IF NOT EXISTS kun_mt_dataset_lifecycle (
	dataset_gid bigint,
	changed jsonb,
    fields jsonb,
	status varchar(64),
	create_at timestamp
);

CREATE TABLE IF NOT EXISTS kun_mt_dataset_snapshot (
    id bigint PRIMARY KEY,
    dataset_gid bigint NOT NULL,
    schema_snapshot JSONB,
    statistics_snapshot JSONB,
    schema_at TIMESTAMP,
    statistics_at TIMESTAMP
);
CREATE INDEX index_snapshot_dataset_gid_schema_at ON kun_mt_dataset_snapshot(dataset_gid, schema_at);
CREATE INDEX index_snapshot_dataset_gid_statistics_at ON kun_mt_dataset_snapshot(dataset_gid, statistics_at);