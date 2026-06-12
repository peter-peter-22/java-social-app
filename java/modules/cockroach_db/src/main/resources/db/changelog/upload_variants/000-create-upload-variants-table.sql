--liquibase formatted sql

--changeset me:1
create table if not exists upload_variants (
    origin_id uuid not null references uploads (id) ON DELETE CASCADE,
    bucket_name varchar(32) not null,
    file_extension varchar(32) not null,
    variant_name varchar(32) not null,
    created_at timestamptz not null default now(),
    status varchar(32) not null,
    PRIMARY KEY (origin_id, variant_name)
);

--rollback drop table if exists uploads; drop index if exists uploads_timestamp;