--liquibase formatted sql

--changeset me:1
create table if not exists lazy_transformation_sessions (
    upload_id uuid primary key references uploads (id) ON DELETE CASCADE
    created_at timestamptz not null default now(),
);

--rollback drop table if exists lazy_transformation_sessions;