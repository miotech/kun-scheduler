alter table kun_dq_case_metrics rename column dq_case_id to id;

alter table kun_dq_case_metrics
    add case_id bigint default -1 not null;

update kun_dq_case_metrics set case_id = id;