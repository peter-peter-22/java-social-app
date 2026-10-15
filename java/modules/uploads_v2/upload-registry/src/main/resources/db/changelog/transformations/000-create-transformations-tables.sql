--liquibase formatted sql

--changeset me:1
create table if not exists named_transformations(
    id uuid primary key default gen_random_uuid(),
    name varchar(50) not null unique,
    created_at timestamptz not null default now(),
    last_updated_at timestamptz not null default now(),
    current_version_id uuid
);

create table if not exists transformation_versions (
    id uuid primary key default gen_random_uuid(),
    named_transformation_id uuid not null references named_transformations(id),
    operations jsonb not null,
    created_at timestamptz not null default now(),

    unique (named_transformation_id, operations)
);

ALTER TABLE named_transformations
ADD CONSTRAINT fk_named_transformation_current_version
FOREIGN KEY (current_version_id)
REFERENCES transformation_versions (id);

--rollback drop table if exists named_transformations; drop table if exists transformation_versions;