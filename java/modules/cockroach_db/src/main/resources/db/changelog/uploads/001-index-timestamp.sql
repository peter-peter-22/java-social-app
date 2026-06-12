create index if not exists uploads_timestamp on uploads (created_at);

--rollback drop index if exists uploads_timestamp;