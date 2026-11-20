--liquibase formatted sql

--changeset me:1
create table if not exists image_metadata(
    asset_id uuid primary key references stored_asset(id) ON DELETE CASCADE,
    resolution_x integer not null,
    resolution_y integer not null
);

--rollback drop table if exists image_metadata;