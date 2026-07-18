package com.example.uploads_api.transformations.upload_repository;

import com.example.uploads_api.uploads.Upload;
import com.example.uploads_api.uploads.UploadId;
import com.example.uploads_api.uploads.UploadStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface UploadRepository {
    @Nullable Upload getById(@NotNull UploadId id);

    @NotNull UploadId create(@NotNull InsertUpload upload) throws UploadMissingUserException;

    @Nullable Upload updateStatus(@NotNull UploadId uploadId, @NotNull UploadStatus status);

    void update(@NotNull Upload save);
}
