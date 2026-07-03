--liquibase formatted sql

--changeset me:1
create table if not exists uploads (
    object_path varchar(64) not null,
    created_by uuid not null references users (id) ON DELETE CASCADE,
    bucket varchar(16) not null,
    file_type varchar(16) not null,
    created_at timestamptz not null default now(),
    status varchar(16) not null,

    PRIMARY KEY (object_path, bucket)
);

--rollback drop table if exists uploads; drop index if exists uploads_timestamp;