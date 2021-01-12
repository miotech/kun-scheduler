CREATE TABLE IF NOT EXISTS kun_mt_dataset_lifecycle (
	dataset_gid bigint,
	changed jsonb,
	status varchar(64),
	create_at timestamp
);