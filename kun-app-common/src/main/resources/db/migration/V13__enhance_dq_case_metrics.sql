alter table kun_dq_case_metrics add rule_records jsonb;

update kun_dq_case_metrics set error_reason = null;