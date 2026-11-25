--liquibase formatted sql

--changeset me:1
create table if not exists upload_permission (
    id uuid primary key default gen_random_uuid(),
    token_hash varchar(64) not null unique,
    upload_key varchar(100) not null,
    extension varchar(20) not null,
    content_type varchar(127),
    max_bytes bigint,
    eager_transformation_names varchar(50)[],
    incoming_transformation_name varchar(50),
    visibility varchar(20) not null,
    expires_at timestamptz not null,
    max_uses int not null,
    uses int not null default 0,
    status varchar(20) not null,
    created_at timestamptz not null default now()
);

create index if not exists upload_permission_token_hash on upload_permission (token_hash);
create index if not exists upload_permission_upload_key on upload_permission (upload_key);

--rollback drop table if exists upload_permission;
