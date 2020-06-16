

create table kun_mt_dataset_gid
(
    data_store  jsonb  not null,
    dataset_gid bigint not null
);

create table kun_mt_tag
(
    tag varchar(256) not null
        constraint kun_mt_tag_pkey
            primary key
);

create table kun_mt_datasource_type
(
    id   bigserial    not null
        constraint kun_mt_cluster_type_pk
            primary key,
    name varchar(128) not null
);

create table kun_mt_datasource
(
    id              bigserial not null
        constraint kun_mt_cluster_pkey
            primary key,
    connection_info jsonb,
    type_id         bigint
        constraint kun_mt_cluster_kun_mt_cluster_type_id_fk
            references kun_mt_datasource_type
            on update cascade on delete cascade
);

create table kun_mt_dataset
(
    gid           bigint        not null
        constraint kun_mt_dataset_pkey
            primary key,
    name          varchar(1024) not null,
    datasource_id bigint        not null
        constraint kun_mt_dataset_cluster_id_fkey
            references kun_mt_datasource
            on update cascade on delete cascade,
    schema        jsonb,
    data_store    jsonb
);

create table kun_mt_dataset_field
(
    id          bigserial     not null
        constraint kun_mt_dataset_field_pkey
            primary key,
    dataset_gid bigint        not null
        constraint kun_mt_dataset_field_dataset_gid_fkey
            references kun_mt_dataset
            on update cascade on delete cascade,
    name        varchar(1024) not null,
    type        varchar(64)   not null,
    description varchar(16384),
    raw_type    varchar(16384)
);

create table kun_mt_dataset_stats
(
    id          bigserial not null
        constraint kun_mt_dataset_stats_pkey
            primary key,
    dataset_gid bigint    not null
        constraint kun_mt_dataset_stats_dataset_gid_fkey
            references kun_mt_dataset
            on update cascade on delete cascade,
    stats_date  timestamp not null,
    row_count   bigint    not null,
    updator     varchar(1024)
);

create table kun_mt_dataset_field_stats
(
    id             bigserial not null
        constraint kun_mt_dataset_field_stats_pkey
            primary key,
    field_id       bigint    not null
        constraint kun_mt_dataset_field_stats_field_id_fkey
            references kun_mt_dataset_field
            on update cascade on delete cascade,
    stats_date     timestamp not null,
    distinct_count bigint    not null,
    nonnull_count  bigint    not null,
    updator        varchar(1024)
);

create table kun_mt_dataset_relations
(
    upstream_dataset_gid   bigint not null
        constraint kun_mt_dataset_relations_upstream_dataset_gid_fkey
            references kun_mt_dataset
            on update cascade on delete cascade,
    downstream_dataset_gid bigint not null
        constraint kun_mt_dataset_relations_downstream_dataset_gid_fkey
            references kun_mt_dataset
            on update cascade on delete cascade,
    task_id                bigint not null,
    constraint kun_mt_dataset_relations_pkey
        primary key (upstream_dataset_gid, downstream_dataset_gid)
);

create table kun_mt_datasource_attrs
(
    datasource_id bigint        not null
        constraint kun_mt_cluster_attrs_pkey
            primary key,
    name          varchar(1024) not null,
    create_user   varchar(256)  not null,
    create_time   timestamp     not null,
    update_user   varchar(256)  not null,
    update_time   timestamp     not null,
    constraint kun_mt_cluster_attrs_cluster_id_fkey foreign key (datasource_id)
        references kun_mt_datasource
        on update cascade on delete cascade
);

create table kun_mt_datasource_tags
(
    id            bigserial    not null
        constraint kun_mt_cluster_tags_pkey
            primary key,
    datasource_id bigint       not null
        constraint kun_mt_cluster_tags_cluster_id_fkey
            references kun_mt_datasource
            on update cascade on delete cascade,
    tag           varchar(256) not null,
    constraint kun_mt_datasource_tags_datasource_id_tag_key
        unique (datasource_id, tag)
);

create table kun_mt_dataset_attrs
(
    dataset_gid bigint not null
        constraint kun_mt_dataset_attrs_pkey
            primary key,
    description varchar(16384),
    constraint kun_mt_dataset_attrs_dataset_gid_fkey foreign key (dataset_gid)
        references kun_mt_dataset
        on update cascade on delete cascade
);

create table kun_mt_dataset_owners
(
    id          bigserial    not null
        constraint kun_mt_dataset_owners_pkey
            primary key,
    dataset_gid bigint       not null
        constraint kun_mt_dataset_owners_dataset_gid_fkey
            references kun_mt_dataset
            on update cascade on delete cascade,
    owner       varchar(256) not null
);

create table kun_mt_dataset_tags
(
    id          bigserial    not null
        constraint kun_mt_dataset_tags_pkey
            primary key,
    dataset_gid bigint       not null
        constraint kun_mt_dataset_tags_dataset_gid_fkey
            references kun_mt_dataset
            on update cascade on delete cascade,
    tag         varchar(256) not null,
    constraint kun_mt_dataset_tags_dataset_gid_tag_key
        unique (dataset_gid, tag)
);

create table kun_mt_datasource_type_fields
(
    id             bigserial         not null
        constraint kun_mt_cluster_type_fields_pk
            primary key,
    type_id        bigint         not null,
    name           varchar(128)      not null,
    sequence_order integer default 0 not null,
    format         varchar(32)       not null,
    constraint kun_mt_cluster_type_fields_kun_mt_cluster_type_id_fk foreign key (type_id) references kun_mt_datasource_type
        on update cascade on delete cascade
);
