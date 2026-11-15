--liquibase formatted sql

--changeset me:1
create table if not exists upload_process (
    upload_key varchar(100) primary key,
    active_attempt_id uuid,
    upload_id uuid references upload(id)
);

create table if not exists upload_attempt (
    attempt_id uuid primary key default gen_random_uuid(),
    upload_key varchar(100) not null references upload_process(upload_key) on delete cascade,
    eager_transformation_names varchar(50)[],
    incoming_transformation_name varchar(50),
    original_asset_id uuid not null references stored_asset(id),
    created_at timestamptz not null default now(),
    status varchar(20) not null,
    finished_at timestamptz
);

alter table upload_process
add constraint fk_upload_process_active_attempt
foreign key (active_attempt_id)
references upload_attempt (attempt_id);

--rollback alter table upload_process drop constraint if exists fk_upload_process_active_attempt; drop table if exists upload_attempt; drop table if exists upload_process;
