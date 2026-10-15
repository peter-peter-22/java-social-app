--liquibase formatted sql

--changeset me:1
create table if not exists upload (
    upload_key varchar(100) unique not null,
    id uuid primary key default gen_random_uuid(),
    asset_id uuid not null unique references stored_asset(id),
    created_at timestamptz not null default now()
);

--rollback drop table if exists upload;