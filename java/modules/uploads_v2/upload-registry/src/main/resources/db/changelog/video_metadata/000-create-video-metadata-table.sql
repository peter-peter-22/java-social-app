--liquibase formatted sql

--changeset me:1
create table if not exists video_metadata (
    upload_id uuid not null references upload(id) ON DELETE CASCADE,
    resolution_x integer not null,
    resolution_y integer not null,
    duration_seconds float not null
);

--rollback drop table if exists video_metadata;