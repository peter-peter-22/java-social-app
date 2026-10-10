--liquibase formatted sql

--changeset me:1
create table if not exists transformation_versions (
    id uuid primary key default gen_random_uuid(),
    named_transformation_id uuid not null references named_transformations(id),
    parameters jsonb not null,
    created_at timestamptz not null default now(),

    unique (named_transformation_id, parameters)
);

--rollback drop table if exists transformation_versions;