create table kun_dq_case_template
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

alter table kun_dq_case_template
    owner to postgres;

create table kun_dq_case_datasource_template
(
    id                 bigserial      not null
        constraint kun_dq_case_template_to_datasource_type_ref_pk
            primary key,
    template_id        bigint         not null
        constraint kun_dq_case_datasource_template_template_id_fk
            references kun_dq_case_template
            on update cascade on delete cascade,
    datasource_type_id bigint         not null
        constraint kun_dq_case_datasource_template_datasource_type_id_fk
            references kun_mt_datasource_type
            on update cascade on delete cascade,
    execution_string   varchar(10000) not null
);

alter table kun_dq_case_datasource_template
    owner to postgres;

create table kun_dq_case
(
    id               bigserial      not null
        constraint kun_dq_rule_pk
            primary key,
    name             varchar(128)   not null,
    description      varchar(10000) not null,
    dataset_id       bigint         not null
        constraint kun_dq_case_kun_mt_dataset_gid_fk
            references kun_mt_dataset
            on update cascade on delete cascade,
    dataset_field_id bigint
        constraint kun_dq_case_kun_mt_dataset_field_id_fk
            references kun_mt_dataset_field
            on update cascade on delete cascade,
    template_id      bigint
        constraint kun_dq_case_kun_dq_case_datasource_template_id_fk
            references kun_dq_case_datasource_template
            on update cascade on delete cascade,
    execution_string varchar(10000),
    create_user      varchar(1024)  not null,
    create_time      timestamp      not null,
    update_user      varchar(1024)  not null,
    update_time      timestamp      not null
);

alter table kun_dq_case
    owner to postgres;

create table kun_dq_case_associated_dataset
(
    id         bigserial not null
        constraint kun_dq_case_associated_dataset_pk
            primary key,
    case_id    bigint    not null
        constraint kun_dq_case_associated_dataset_kun_dq_case_id_fk
            references kun_dq_case
            on update cascade on delete cascade,
    dataset_id bigint    not null
        constraint kun_dq_case_associated_dataset_kun_mt_dataset_gid_fk
            references kun_mt_dataset
            on update cascade on delete cascade
);

alter table kun_dq_case_associated_dataset
    owner to postgres;

create table kun_dq_case_rules
(
    id                  bigserial     not null
        constraint kun_dq_case_rules_pk
            primary key,
    case_id             bigint        not null
        constraint kun_dq_case_rules_kun_dq_case_id_fk
            references kun_dq_case
            on update cascade on delete cascade,
    field               varchar(1024) not null,
    operator            varchar(64)   not null,
    expected_value_type varchar(64)   not null,
    expected_value      varchar(1024) not null
);

alter table kun_dq_case_rules
    owner to postgres;

