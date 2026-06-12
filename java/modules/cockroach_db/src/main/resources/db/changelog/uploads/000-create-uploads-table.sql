--liquibase formatted sql

--changeset me:1
create table if not exists uploads (
    id uuid primary key default gen_random_uuid(),
    created_by uuid not null,
    bucket_name varchar(32) not null,
    media_type varchar(32) not null,
    created_at timestamptz not null default now(),
    transformation_group varchar(32) not null,
    transformation_version smallint not null,
    status varchar(32) not null,
    file_extension varchar(32) not null
);

--rollback drop table if exists uploads; drop index if exists uploads_timestamp;