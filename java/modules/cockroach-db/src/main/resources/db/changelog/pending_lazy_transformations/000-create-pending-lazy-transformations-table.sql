--liquibase formatted sql

--changeset me:1
create table if not exists pending_lazy_transformations (
    session_id uuid primary key references lazy_upload_sessions (id) ON DELETE CASCADE,
    transformation_name varchar(32) not null,
);

--rollback drop table if exists pending_lazy_transformations;