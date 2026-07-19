--liquibase formatted sql

--changeset me:1
create table if not exists uploads (
    id uuid primary key default gen_random_uuid(),
    object_path varchar(100) not null,
    bucket varchar(32) not null,
    created_by uuid not null ON DELETE CASCADE,
    file_type varchar(8) not null,
    created_at timestamptz not null default now(),
    status varchar(16) not null,

    unique (object_path, bucket)
    constraint fk_uploads_user FOREIGN KEY created_by REFERENCES users
(
    id
)
);

--rollback drop table if exists uploads;