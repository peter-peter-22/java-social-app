package com.example.uploads.upload_variant_repository;

import com.example.uploads.upload_repository.*;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
class UploadVariantRepositoryImpl implements UploadVariantRepository {
    private final UploadVariantCrudRepository crud;
    private final JdbcClient jdbc;
    private final JdbcAggregateTemplate template;

    private static @NotNull UploadVariant entityToDomain(@NotNull UploadVariantEntity entity) {
        return new UploadVariant(
                new UploadVariantId(new UploadId(entity.key().originId()),entity.key().variantName()),
                entity.bucketName(),
                entity.fileExtension(),
                entity.createdAt(),
                entity.status()
        );
    }

    private static @NotNull UploadVariantEntity domainToEntity(@NotNull UploadVariant domain) {
        return new UploadVariantEntity(
                new UploadVariantEntityId(domain.key().originId().get(), domain.key().variantName()),
                domain.bucketName(),
                domain.fileExtension(),
                domain.createdAt(),
                domain.status()
        );
    }

    @Override
    public @Nullable UploadVariant getById(@NotNull UploadVariantId id) {
        return crud.findById(new UploadVariantEntityId(id.originId().get(), id.variantName()))
                .map(UploadVariantRepositoryImpl::entityToDomain)
                .orElse(null);
    }

    @Override
    public @NotNull UploadVariantId create(@NotNull InsertUploadVariant upload) {
        var id = jdbc.sql("""
                        INSERT INTO upload_variants (origin_id, bucket_name, file_extension, variant_name, status)
                        VALUES (:origin_id, :bucket_name, :file_extension, :variant_name, :status)
                        RETURNING origin_id, variant_name
                        """)
                .param("origin_id", upload.key().originId().get())
                .param("bucket_name", upload.bucketName())
                .param("file_extension", upload.fileExtension())
                .param("variant_name", upload.key().variantName())
                .param("status", upload.status().name())
                .query(UploadVariantEntityId.class)
                .single();
        return new UploadVariantId(new UploadId(id.originId()), id.variantName());
    }

    @Override
    public void update(@NotNull UploadVariant save) {
        var e = domainToEntity(save);
        template.update(e);
    }
}
