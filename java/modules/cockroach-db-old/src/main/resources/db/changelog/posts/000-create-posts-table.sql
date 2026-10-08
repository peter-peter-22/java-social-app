--liquibase formatted sql

--changeset me:1
create table if not exists posts (
    id uuid primary key default gen_random_uuid(),
    author_id uuid not null references users (id) ON DELETE CASCADE,
    body text not null
);

--rollback drop if exists table posts;