--liquibase formatted sql

--changeset me:1
create table if not exists users (
    id uuid primary key default gen_random_uuid()
);

--rollback
drop table users;