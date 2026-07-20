package com.example.uploads_api.transformations.upload_repository;

import com.example.uploads_api.uploads.FileType;
import com.example.uploads_api.uploads.UploadStatus;
import com.example.users_api.repository.UserId;
import lombok.Builder;
import lombok.Getter;
import org.jspecify.annotations.NonNull;

@Builder
@Getter
public class InsertUpload {
    private final @NonNull String bucket;
    private final @NonNull String objectPath;
    private final @NonNull FileType fileType;
    private final @Builder.Default
    @NonNull UploadStatus status = UploadStatus.UPLOADING;
    private final @NonNull UserId createdBy;
}
