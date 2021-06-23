-- V1__init_ddl.sql

create table if not exists kun_mt_dataset_attrs (
    dataset_gid bigint primary key,
    description varchar(16384)
);

create table if not exists kun_mt_dataset_owners (
    id           bigserial primary key,
    dataset_gid  bigint not null,
    owner        varchar(256) not null
);

create table if not exists kun_mt_dataset_tags
(
    id            bigserial primary key,
    dataset_gid   bigint not null,
    tag           varchar(256) not null,
    constraint kun_mt_dataset_tags_dataset_gid_tag_key unique (dataset_gid, tag)
);

create table if not exists kun_mt_glossary
(
    id          bigserial     primary key,
    name        varchar(1024) not null,
    description varchar(10000),
    parent_id   bigint,
    create_user varchar(256)  not null,
    create_time timestamp     not null,
    update_user varchar(256)  not null,
    update_time timestamp     not null,
    prev_id bigint
);

create table if not exists kun_mt_glossary_to_dataset_ref
(
    id          bigserial primary key,
    glossary_id bigserial not null,
    dataset_id  bigserial not null,
    constraint kun_mt_glossary_to_dataset_ref_glossary_id_dataset_id unique (glossary_id, dataset_id)
);

-- V2__data_quality_ddl.sql

create table if not exists kun_dq_case_template
(
    id          bigserial    not null
        constraint kun_dq_rule_template_pk
            primary key,
    name        varchar(256) not null,
    type        varchar(128) not null,
    create_user varchar(128) not null,
    create_time timestamp    not null,
    update_user varchar(128) not null,
    update_time timestamp    not null
);

comment on column kun_dq_case_template.type is 'column or table';

create table if not exists kun_dq_case_datasource_template
(
    id                 bigserial      primary key,
    template_id        bigint         not null,
    datasource_type_id bigint         not null,
    execution_string   varchar(10000) not null
);

create table if not exists kun_dq_case
(
    id               bigserial      primary key,
    name             varchar(128)   not null,
    description      varchar(10000) not null,
    task_id          bigint,
    template_id      bigint,
    execution_string varchar(10000),
    types            text,
    create_user      varchar(1024)  not null,
    create_time      timestamp      not null,
    update_user      varchar(1024)  not null,
    update_time      timestamp      not null,
    primary_dataset_id bigint
);

create table if not exists kun_dq_case_associated_dataset
(
    id          bigserial  primary key,
    case_id     bigint     not null,
    dataset_id  bigint     not null
);

create table if not exists kun_dq_case_rules
(
    id                  bigserial     primary key,
    case_id             bigint        not null,
    field               varchar(1024) not null,
    operator            varchar(64)   not null,
    expected_value_type varchar(64)   not null,
    expected_value      varchar(1024) not null
);

-- V3__data_quality_fix_ddl.sql

-- add many to many table
create table if not exists kun_dq_case_associated_dataset_field
(
    id               bigint primary key,
    case_id          bigint not null,
    dataset_field_id bigint not null
);

-- add data quality task instance history table
create table if not exists kun_dq_case_task_history
(
    id           bigint      primary key,
    task_id      bigint      not null,
    task_run_id  bigint      not null,
    case_id      bigint      not null,
    case_status  varchar(50) not null,
    error_reason text,
    start_time   timestamp   not null,
    end_time     timestamp
);

-- V4__data_quality_fix_ddl.sql

-- V5__add_pg_trgm_ddl.sql

-- create extension if not exists pg_trgm;

-- V6__add_test_case_id_unique_constraint.sql

create unique index if not exists kun_dq_case_task_history_task_run_id_uindex on kun_dq_case_task_history (task_run_id);

-- V7__remove_dataset_cascade.sql

-- V8__remove_foreign_key.sql

-- V9__add_dq_case_types.sql

create table if not exists kun_dq_case_metrics
(
    id                       bigint primary key,
    error_reason             text,
    continuous_failing_count bigint default 0 not null,
    update_time              timestamp not null,
    rule_records             jsonb default null,
    case_id                  bigint default -1 not null
);

-- V10__add_glossary_order.sql

-- update kun_mt_glossary kmg
-- set prev_id = kmg_next.next_id from (select id,
--              lead(id) over (order by parent_id, create_time)        as next_id,
--              lead(parent_id) over (order by parent_id, create_time) as next_parent_id
--       from kun_mt_glossary
--       order by parent_id, create_time) kmg_next
-- where kmg.id = kmg_next.id and (kmg.parent_id = kmg_next.next_parent_id or kmg.parent_id is null);

-- V11__add_dq_case_types.sql

-- V12__add_dq_case_primary_dataset.sql

-- alter table kun_dq_case add types text;

-- V13__enhance_dp

-- V14__drop_useless_security_table.sql

drop table if exists kun_user;

drop table if exists kun_user_session_attributes;

drop table if exists kun_user_session;

-- V15__enhance_dq_case_metrics.sql
