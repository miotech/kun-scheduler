create table if not exists kun_security_role (
    module varchar(256) not null,
    name varchar(1024) not null,
    description varchar,
    constraint uk_role_name unique(name, module)
);


create table if not exists kun_security_user_role_scope (
    username varchar(1024) not null,
    module varchar(256) not null,
    rolename varchar(1024) not null,
    source_system_id varchar(256) not null,
    constraint uk_user_role_scope unique(username, module, rolename, source_system_id)
);