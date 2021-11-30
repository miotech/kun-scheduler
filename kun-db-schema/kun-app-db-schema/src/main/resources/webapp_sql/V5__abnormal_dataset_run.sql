CREATE TABLE IF NOT EXISTS kun_dq_abnormal_dataset (
    id BIGSERIAL PRIMARY KEY,
    dataset_gid BIGINT NOT NULL,
    task_run_id BIGINT NOT NULL,
    task_id BIGINT NOT NULL,
    task_name varchar(1024) NOT NULL,
    create_time timestamp NOT NULL,
    update_time timestamp NOT NULL,
    schedule_at varchar(64) NOT NULL,
    status varchar(64)
);