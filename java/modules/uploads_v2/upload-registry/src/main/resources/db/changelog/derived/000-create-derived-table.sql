--liquibase formatted sql

--changeset me:1
create table if not exists derived
(
    upload_id
    uuid
    not
    null
    references
    upload
(
    internal_id
) ON DELETE CASCADE,
    transformation_id uuid not null references named_transformations
(
    internal_id
),
    created_at timestamptz not null default now
(
)
    );

--rollback drop table if exists derived;