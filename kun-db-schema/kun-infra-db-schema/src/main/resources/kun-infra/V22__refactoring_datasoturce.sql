ALTER TABLE  kun_mt_datasource DROP COLUMN  IF  EXISTS  connection_info;
ALTER TABLE  kun_mt_datasource DROP COLUMN  IF  EXISTS connection_config;
ALTER TABLE  kun_mt_datasource DROP COLUMN  IF  EXISTS type_id ;
ALTER TABLE  kun_mt_datasource ADD COLUMN  IF NOT EXISTS  datasource_config jsonb  null;
ALTER TABLE  kun_mt_datasource ADD COLUMN  IF NOT EXISTS dsi varchar   NULL;


CREATE TABLE IF NOT EXISTS kun_mt_connection_info (
    "id"  bigint primary key,
    "datasource_id" bigint not null,
    "name" varchar  not NULL,
    "conn_scope" varchar  not NULL,
    "conn_config" jsonb not null,
    "description" varchar   NULL,
    "update_time" timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "update_user" varchar  not NULL,
    "create_time" timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "create_user" varchar  not NULL,
    "deleted" bool NULL DEFAULT false
);
CREATE INDEX IF NOT EXISTS kun_mt_connection_datasource_id_idx ON public.kun_mt_connection_info (datasource_id);

