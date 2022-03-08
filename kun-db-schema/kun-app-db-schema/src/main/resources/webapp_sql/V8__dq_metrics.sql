create table kun_dq_metrics_collection (
expectation_id bigint,
execution_result jsonb,
collect_at timestamp
);

alter table kun_dq_expectation add column metrics_config jsonb;
alter table kun_dq_expectation add column assertion_config jsonb;