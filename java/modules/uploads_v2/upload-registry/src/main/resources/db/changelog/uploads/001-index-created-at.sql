--liquibase formatted sql

--changeset me:1
create index if not exists uploads_created_at_idx on upload (created_at);

--rollback drop index if exists uploads_created_at_idx;