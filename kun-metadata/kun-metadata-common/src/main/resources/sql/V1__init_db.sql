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
