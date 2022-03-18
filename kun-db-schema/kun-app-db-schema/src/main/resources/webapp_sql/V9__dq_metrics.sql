create table if not exists kun_dq_metrics_collection (
id bigserial primary key,
expectation_id bigint,
execution_result jsonb,
collected_at timestamp
);

alter table kun_dq_expectation add column if not exists metrics_config jsonb;
alter table kun_dq_expectation add column if not exists assertion_config jsonb;