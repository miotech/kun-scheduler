alter table kun_dq_expectation add column case_type varchar(64) not null default 'SKIP';
alter table kun_dq_expectation drop is_blocking;
alter table kun_dq_case_run add column validate_version varchar(64);
alter table kun_dq_case_run add column validate_dataset_id int8 ;