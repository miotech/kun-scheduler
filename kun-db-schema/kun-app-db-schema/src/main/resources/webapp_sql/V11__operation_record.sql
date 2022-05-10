create table if not exists kun_operation_record(
id bigserial primary key,
operator varchar(1024) not null,
type varchar(1024) not null,
event jsonb not null,
status varchar(64) not null,
create_time timestamp not null,
update_time timestamp not null
)