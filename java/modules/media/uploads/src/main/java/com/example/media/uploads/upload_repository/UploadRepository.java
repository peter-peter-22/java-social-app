package com.example.media.uploads.upload_repository;

import com.example.cockroach_db.SQLErrorCodes;
import com.example.media.api.uploads.Upload;
import com.example.media.api.uploads.UploadId;
import com.example.media.api.uploads.UploadStatus;
import com.example.users.persistence.repository.UserId;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;

@Repository
@RequiredArgsConstructor
public class UploadRepository {
    private final JdbcClient jdbc;
    private final JdbcAggregateTemplate template;

    private static Upload entityToDomain(@NotNull UploadEntity entity) {
        return new Upload(
                new UploadId(entity.id().objectPath(), entity.id().bucket()),
                new UserId(entity.createdBy()),
                entity.fileType(),
                entity.createdAt(),
                entity.status()
        );
    }

    private static UploadEntity domainToEntity(@NotNull Upload domain) {
        return new UploadEntity(
                new UploadEntityId(domain.id().objectPath(), domain.id().bucket()),
                domain.createdBy().get(),
                domain.fileType(),
                domain.createdAt(),
                domain.status()
        );
    }

    public @Nullable Upload getById(@NotNull UploadId id) {
        var entity = template.findById(new UploadEntityId(id.objectPath(), id.bucket()), UploadEntity.class);
        if (entity == null) {
            return null;
        }
        return entityToDomain(entity);
    }

    public @NotNull UploadId create(@NotNull InsertUpload upload) throws UploadMissingUserException {
        try {
            var id = jdbc.sql("""
                            INSERT INTO uploads (created_by, object_path, bucket, file_type, status)
                            VALUES (:created_by, :object_path, :bucket, :file_type, :status)
                            RETURNING object_path, bucket
                            """)
                    .param("object_path", upload.getObjectPath())
                    .param("created_by", upload.getCreatedBy().get())
                    .param("bucket", upload.getBucket())
                    .param("file_type", upload.getFileType().name())
                    .param("status", upload.getStatus().name())
                    .query(UploadEntityId.class)
                    .single();
            return new UploadId(id.objectPath(), id.bucket());
        } catch (DataIntegrityViolationException e) {
            if (e.getCause() instanceof SQLException cause && cause.getSQLState().equals(SQLErrorCodes.FOREIGN_KEY_VIOLATION)) {
                throw new UploadMissingUserException(cause.getMessage());
            }
            throw e;
        }
    }

    public void update(@NotNull Upload save) {
        var e = domainToEntity(save);
        template.save(e);
    }

    /**
     * Updates the status field of an upload if it matches the previous status.
     * Returns the updated upload or null if the upload was not found.
     */
    public @Nullable Upload updateStatus(@NotNull UploadId uploadId, @NotNull UploadStatus status, @NotNull UploadStatus previousStatus) {
        var updated = jdbc.sql("""
                        UPDATE uploads
                        SET status = :status
                        WHERE object_path = :object_path AND bucket = :bucket AND status = :previous_status
                        RETURNING *
                        """)
                .param("object_path", uploadId.objectPath())
                .param("bucket", uploadId.bucket())
                .param("status", status.name())
                .param("previous_status", previousStatus.name())
                .query(UploadEntity.class)
                .optional();
        return updated.map(UploadRepository::entityToDomain).orElse(null);
    }
}
