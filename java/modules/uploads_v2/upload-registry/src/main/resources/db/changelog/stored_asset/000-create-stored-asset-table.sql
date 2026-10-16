--liquibase formatted sql

--changeset me:1
create table if not exists stored_asset (
    id uuid primary key default gen_random_uuid(),
    home_region varchar(50) not null,
    object_bucket varchar(50) not null,
    object_path varchar(50) not null,
    directory boolean not null,
    entry_point varchar(50) not null,
    mime_type varchar(50) not null,
    extension varchar(10),
    created_at timestamptz not null default now(),
    bytes integer not null,
    delivery_protocol varchar(20)
);

--rollback drop table if exists stored_asset;