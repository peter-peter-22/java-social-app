--liquibase formatted sql

--changeset me:1
create index if not exists named_transformation_name_created_at_idx on named_transformations (name, created_at);

--rollback drop index if exists named_transformation_name_created_at_idx;