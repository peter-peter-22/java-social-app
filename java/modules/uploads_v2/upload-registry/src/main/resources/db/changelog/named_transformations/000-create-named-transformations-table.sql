--liquibase formatted sql

--changeset me:1
create table if not exists named_transformations
(
    internal_id
    uuid
    primary
    key
    default
    gen_random_uuid
(
),
    name varchar
(
    50
) not null,
    parameters varchar
(
    200
) not null ,
    created_at timestamptz not null default now
(
),
    syntax_version smallint not null
    );

--rollback drop table if exists named_transformations;