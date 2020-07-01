CREATE TABLE IF NOT EXISTS kun_mt_datasource_type (
	id bigserial primary key,
	name varchar(128) not null
);

CREATE TABLE IF NOT EXISTS kun_mt_datasource (
	id bigserial primary key,
	connection_info jsonb,
	type_id bigint
);