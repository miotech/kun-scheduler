create table kun_dq_case_metrics
(
    dq_case_id bigint
        constraint kun_dq_case_metrics_pk
            primary key,
    error_reason text,
    continuous_failing_count bigint default 0 not null,
    update_time timestamp not null
);