--liquibase formatted sql

--changeset me:1
create table if not exists upload (
    upload_key varchar(100) unique not null,
    home_region varchar(20) not null,
    id uuid primary key default gen_random_uuid(),
    mime_type varchar(50) not null,
    created_at timestamptz not null default now(),
    bytes integer not null
);

--rollback drop table if exists upload;