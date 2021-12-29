create table if not exists kun_wf_worker_image(
    "id" bigint primary key,
    "image_name" varchar(1024) NOT NULL,
    "version" varchar(64) NOT NULL,
    "active" boolean not null,
    "created_at" timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updated_at" timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP
)