package com.example.uploads_api.transformations.upload_repository;

import com.example.uploads_api.uploads.Upload;
import com.example.uploads_api.uploads.UploadId;
import com.example.uploads_api.uploads.UploadStatus;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public interface UploadRepository {
    @Nullable Upload getById(@NonNull UploadId id);

    @NonNull UploadId create(@NonNull InsertUpload upload) throws UploadMissingUserException;

    @Nullable Upload updateStatus(@NonNull UploadId uploadId, @NonNull UploadStatus status);

    void update(@NonNull Upload save);
}
