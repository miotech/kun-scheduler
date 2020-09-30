create table kun_user
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