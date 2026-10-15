--liquibase formatted sql

--changeset me:1
create table if not exists variant (
    upload_id uuid not null references upload (id) ON DELETE CASCADE,
    transformation_version_id uuid not null references transformation_versions(id),
    created_at timestamptz not null default now(),

    primary key (upload_id, transformation_version_id)
);

--rollback drop table if exists variant;