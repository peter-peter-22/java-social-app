package com.example.uploads.upload_repository;

import com.example.api.Upload;
import com.example.api.UploadId;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface UploadRepository {
    @Nullable Upload getById(@NotNull UploadId id);
    @NotNull UploadId create(@NotNull InsertUpload upload) throws UploadMissingUserException;
    void update(@NotNull Upload save);
}
