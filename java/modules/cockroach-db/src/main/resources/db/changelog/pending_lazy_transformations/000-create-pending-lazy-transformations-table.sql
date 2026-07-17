--liquibase formatted sql

--changeset me:1
create table if not exists pending_lazy_transformations (
    session_id uuid not null references lazy_transformation_sessions (upload_id) ON DELETE CASCADE,
    transformation_name varchar(32) not null,
    primary key (session_id, transformation_name)
);

--rollback drop table if exists pending_lazy_transformations;
