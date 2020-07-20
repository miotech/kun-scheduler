-- drop dataset_field_id from kun_dq_case
alter table kun_dq_case
    drop constraint kun_dq_case_kun_mt_dataset_field_id_fk;

alter table kun_dq_case
    drop column dataset_field_id;

-- add many to many table
create table kun_dq_case_associated_dataset_field
(
    id               bigint
        constraint kun_dq_case_associated_dataset_field_pk
            primary key,
    case_id          bigint not null
        constraint kun_dq_case_associated_dataset_field_case_id_fk
            references kun_dq_case
            on update cascade on delete cascade,
    dataset_field_id bigint not null
        constraint kun_dq_case_associated_dataset_field_dataset_field_id_fk
            references kun_mt_dataset_field
            on update cascade on delete cascade
);

-- add workflow task id column
alter table kun_dq_case
    add task_id bigint;

-- add data quality task instance history table
create table kun_dq_case_task_history
(
    id           bigint
        constraint kun_dq_case_task_history_pk
            primary key,
    task_id      bigint      not null,
    task_run_id  bigint      not null,
    case_id      bigint      not null,
    case_status  varchar(50) not null,
    error_reason text,
    start_time   timestamp   not null,
    end_time     timestamp
);

