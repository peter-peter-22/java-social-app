package com.example.uploads_persistence.upload_repository;

import com.example.media_api.uploads.FileType;
import com.example.media_api.uploads.UploadStatus;
import com.example.users_api.repository.UserId;
import lombok.Builder;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

@Builder
@Getter
public class InsertUpload {
    private final @NotNull String bucket;
    private final @NotNull String objectPath;
    private final @NotNull FileType fileType;
    private final @Builder.Default @NotNull UploadStatus status = UploadStatus.UPLOADING;
    private final @NotNull UserId createdBy;
}
