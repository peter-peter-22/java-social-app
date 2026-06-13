package com.example.uploads.upload_repository;

import com.example.users.repository.UserId;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
@RequiredArgsConstructor
class UploadRepositoryImpl implements UploadRepository {
    private final UploadCrudRepository crud;
    private final JdbcClient jdbc;
    private final JdbcAggregateTemplate template;

    private static Upload entityToDomain(UploadEntity entity) {
        return new Upload(
                new UploadId(entity.id()),
                new UserId(entity.createdBy()),
                entity.bucketName(),
                entity.mediaType(),
                entity.createdAt(),
                entity.transformationGroup(),
                entity.transformationVersion(),
                entity.status(),
                entity.fileExtension()
        );
    }

    private static UploadEntity domainToEntity(Upload domain) {
        return new UploadEntity(
                domain.id().get(),
                domain.createdBy().get(),
                domain.bucketName(),
                domain.mediaType(),
                domain.createdAt(),
                domain.transformationGroup(),
                domain.transformationVersion(),
                domain.status(),
                domain.fileExtension()
        );
    }

    @Override
    public @Nullable Upload getById(@NotNull UploadId id) {
        return crud.findById(id.get())
                .map(UploadRepositoryImpl::entityToDomain)
                .orElse(null);
    }

    @Override
    public @NotNull UploadId create(@NotNull InsertUpload upload) {
        var id = jdbc.sql("""
                        INSERT INTO uploads (created_by, bucket_name, media_type, transformation_group, transformation_version, status, file_extension)
                        VALUES (:created_by, :bucket_name, :media_type, :transformation_group, :transformation_version, :status, :file_extension)
                        RETURNING id
                        """)
                .param("created_by", upload.createdBy().get())
                .param("bucket_name", upload.bucketName())
                .param("media_type", upload.mediaType().name())
                .param("transformation_group", upload.transformationGroup())
                .param("transformation_version", upload.transformationVersion())
                .param("status", upload.status().name())
                .param("file_extension", upload.fileExtension())
                .query(UUID.class)
                .single();
        return new UploadId(id);
    }

    @Override
    public void update(@NotNull Upload save) {
        var e = domainToEntity(save);
        template.save(e);
    }
}
