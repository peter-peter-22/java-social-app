--liquibase formatted sql

--changeset me:1
create table if not exists named_transformations(
    id uuid primary key default gen_random_uuid(),
    name varchar(50) not null unique,
    created_at timestamptz not null default now(),
    last_updated_at timestamptz not null default now(),
    current_version_id uuid
);

--rollback drop table if exists named_transformations;