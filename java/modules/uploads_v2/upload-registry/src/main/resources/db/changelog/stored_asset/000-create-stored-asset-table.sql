--liquibase formatted sql

--changeset me:1
create table if not exists stored_asset (
    id uuid primary key default gen_random_uuid(),
    home_region varchar(50) not null,
    object_bucket varchar(50) not null,
    directory boolean not null,
    entry_point varchar(255) not null,
    mime_type varchar(127) not null,
    extension varchar(20),
    content_type varchar(127) not null,
    created_at timestamptz not null default now(),
    bytes bigint not null,
    delivery_protocol varchar(20) not null,
    status varchar(20) not null default 'UPLOADING',
    objects varchar(255)[] not null
);

--rollback drop table if exists stored_asset;