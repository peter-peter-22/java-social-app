--liquibase formatted sql

--changeset me:1
alter table upload
    add column if not exists visibility varchar(20) not null default 'PUBLIC';

alter table upload_attempt
    add column if not exists visibility varchar(20) not null default 'PUBLIC';

alter table upload_attempt
    add column if not exists upload_permission_id uuid references upload_permission(id);

--rollback alter table upload_attempt drop column if exists upload_permission_id; alter table upload_attempt drop column if exists visibility; alter table upload drop column if exists visibility;
