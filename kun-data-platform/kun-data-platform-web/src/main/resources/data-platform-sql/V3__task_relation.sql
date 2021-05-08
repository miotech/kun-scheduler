create table kun_dp_task_relation
(
	upstream_task_id bigint not null,
	downstream_task_id bigint not null,
	created_at timestamp default CURRENT_TIMESTAMP not null,
	updated_at timestamp default CURRENT_TIMESTAMP not null,
	constraint kun_dp_task_relations_pkey
		primary key (upstream_task_id, downstream_task_id)
);