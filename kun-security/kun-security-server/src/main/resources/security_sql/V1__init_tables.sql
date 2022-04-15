CREATE TABLE IF NOT EXISTS kun_security_user (
    id bigint not null,
    username varchar(1024) not null,
    password VARCHAR(1024),
    external_information jsonb,
    created_at timestamp not null,
    updated_at timestamp not null,
    status varchar(128) not null
);

CREATE UNIQUE INDEX IF NOT EXISTS kun_security_user_username_unique ON kun_security_user (username);

