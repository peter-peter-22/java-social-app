package com.example.uploads.upload_variant_repository;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface UploadVariantRepository {
    @Nullable UploadVariant getById(@NotNull UploadVariantId id);
    @NotNull UploadVariantId create(@NotNull InsertUploadVariant upload);
    void update(@NotNull UploadVariant save);
}
