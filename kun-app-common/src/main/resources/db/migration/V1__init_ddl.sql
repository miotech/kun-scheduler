create table kun_mt_dataset_attrs
(
    dataset_gid bigint not null
        constraint kun_mt_dataset_attrs_pkey
            primary key
        constraint kun_mt_dataset_attrs_kun_mt_dataset_gid_fk
            references kun_mt_dataset
            on update cascade on delete cascade,
    description varchar(16384)
);

alter table kun_mt_dataset_attrs
    owner to postgres;

create table kun_mt_dataset_owners
(
    id          bigserial    not null
        constraint kun_mt_dataset_owners_pkey
            primary key,
    dataset_gid bigint       not null
        constraint kun_mt_dataset_owners_kun_mt_dataset_gid_fk
            references kun_mt_dataset
            on update cascade on delete cascade,
    owner       varchar(256) not null
);

alter table kun_mt_dataset_owners
    owner to postgres;

create table kun_mt_dataset_tags
(
    id          bigserial    not null
        constraint kun_mt_dataset_tags_pkey
            primary key,
    dataset_gid bigint       not null
        constraint kun_mt_dataset_tags_kun_mt_dataset_gid_fk
            references kun_mt_dataset
            on update cascade on delete cascade,
    tag         varchar(256) not null,
    constraint kun_mt_dataset_tags_dataset_gid_tag_key
        unique (dataset_gid, tag)
);

alter table kun_mt_dataset_tags
    owner to postgres;

create table kun_mt_tag
(
    tag varchar(256) not null
        constraint kun_mt_tag_pkey
            primary key
);

alter table kun_mt_tag
    owner to postgres;

create table kun_mt_datasource_type
(
    id   bigserial    not null
        constraint kun_mt_cluster_type_pk
            primary key,
    name varchar(128) not null
);

alter table kun_mt_datasource_type
    owner to postgres;

create table kun_mt_datasource
(
    id              bigserial not null
        constraint kun_mt_cluster_pkey
            primary key,
    connection_info jsonb,
    type_id         bigint
        constraint kun_mt_datasource_kun_mt_datasource_type_id_fk
            references kun_mt_datasource_type
            on update cascade on delete cascade
);

alter table kun_mt_datasource
    owner to postgres;

create table kun_mt_datasource_attrs
(
    datasource_id bigint        not null
        constraint kun_mt_cluster_attrs_pkey
            primary key
        constraint kun_mt_datasource_attrs_kun_mt_datasource_id_fk
            references kun_mt_datasource
            on update cascade on delete cascade,
    name          varchar(1024) not null,
    create_user   varchar(256)  not null,
    create_time   timestamp     not null,
    update_user   varchar(256)  not null,
    update_time   timestamp     not null
);

alter table kun_mt_datasource_attrs
    owner to postgres;

create table kun_mt_datasource_tags
(
    id            bigserial    not null
        constraint kun_mt_cluster_tags_pkey
            primary key,
    datasource_id bigint       not null
        constraint kun_mt_datasource_tags_kun_mt_datasource_id_fk
            references kun_mt_datasource
            on update cascade on delete cascade,
    tag           varchar(256) not null,
    constraint kun_mt_datasource_tags_datasource_id_tag_key
        unique (datasource_id, tag)
);

alter table kun_mt_datasource_tags
    owner to postgres;

create table kun_mt_datasource_type_fields
(
    id             bigserial             not null
        constraint kun_mt_cluster_type_fields_pk
            primary key,
    type_id        bigserial             not null
        constraint kun_mt_datasource_type_fields_kun_mt_datasource_type_id_fk
            references kun_mt_datasource_type
            on update cascade on delete cascade,
    name           varchar(128)          not null,
    sequence_order integer default 0     not null,
    format         varchar(32)           not null,
    require        boolean default false not null
);

alter table kun_mt_datasource_type_fields
    owner to postgres;

create table kun_mt_glossary
(
    id          bigserial     not null
        constraint kun_mt_glossary_pk
            primary key,
    name        varchar(1024) not null,
    description varchar(10000),
    parent_id   bigint
        constraint kun_mt_glossary_kun_mt_glossary_id_fk
            references kun_mt_glossary
            on update cascade on delete cascade,
    create_user varchar(256)  not null,
    create_time timestamp     not null,
    update_user varchar(256)  not null,
    update_time timestamp     not null
);

alter table kun_mt_glossary
    owner to postgres;

create table kun_mt_glossary_to_dataset_ref
(
    id          bigserial not null
        constraint kun_mt_glossary_to_dataset_ref_pk
            primary key,
    glossary_id bigserial not null
        constraint kun_mt_glossary_to_dataset_ref_kun_mt_glossary_id_fk
            references kun_mt_glossary
            on update cascade on delete cascade,
    dataset_id  bigserial not null
        constraint kun_mt_glossary_to_dataset_ref_kun_mt_dataset_gid_fk
            references kun_mt_dataset
            on update cascade on delete cascade,
    constraint kun_mt_glossary_to_dataset_ref_glossary_id_dataset_id
        unique (glossary_id, dataset_id)
);

alter table kun_mt_glossary_to_dataset_ref
    owner to postgres;

create table if not exists kun_user
(
    id   bigint       not null
        constraint kun_user_pk
            primary key,
    name varchar(100) not null
);

alter table kun_user
    owner to postgres;

create unique index if not exists kun_user_name_uindex
    on kun_user (name);

create table if not exists kun_user_session
(
    primary_id            char(36) not null
        constraint user_session_pk
            primary key,
    session_id            char(36) not null,
    creation_time         bigint   not null,
    last_access_time      bigint   not null,
    max_inactive_interval integer  not null,
    expiry_time           bigint   not null,
    principal_name        varchar(100)
);

alter table kun_user_session
    owner to postgres;

create unique index if not exists kun_user_session_ix1
    on kun_user_session (session_id);

create index if not exists kun_user_session_ix2
    on kun_user_session (expiry_time);

create index if not exists kun_user_session_ix3
    on kun_user_session (principal_name);

create table if not exists kun_user_session_attributes
(
    session_primary_id char(36)     not null
        constraint kun_user_session_attributes_fk
            references kun_user_session
            on delete cascade,
    attribute_name     varchar(200) not null,
    attribute_bytes    bytea        not null,
    constraint kun_user_session_attributes_pk
        primary key (session_primary_id, attribute_name)
);

alter table kun_user_session_attributes
    owner to postgres;