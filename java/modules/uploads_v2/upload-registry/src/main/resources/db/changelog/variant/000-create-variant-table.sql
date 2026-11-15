--liquibase formatted sql

--changeset me:1
create table if not exists variant (
    upload_id uuid not null references upload(id),
    named_transformation_id uuid not null references named_transformations(id),
    transformation_version_id uuid not null references transformation_versions(id),
    created_at timestamptz not null default now(),
    asset_id uuid not null unique references stored_asset(id),
    primary key (upload_id, named_transformation_id)
);

--rollback drop table if exists variant;
