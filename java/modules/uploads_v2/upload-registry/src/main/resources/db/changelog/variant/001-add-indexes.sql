--liquibase formatted sql

--changeset me:2
CREATE INDEX IF NOT EXISTS idx_variant_status ON variant (status);
CREATE INDEX IF NOT EXISTS idx_variant_named_transformation_id ON variant (named_transformation_id);
CREATE INDEX IF NOT EXISTS idx_variant_upload_id ON variant (upload_id);

--rollback DROP INDEX IF EXISTS idx_variant_status;
--rollback DROP INDEX IF EXISTS idx_variant_named_transformation_id;
--rollback DROP INDEX IF EXISTS idx_variant_upload_id;
