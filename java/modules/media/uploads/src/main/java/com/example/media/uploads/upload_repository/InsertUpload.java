package com.example.media.uploads.upload_repository;

import com.example.media.common.uploads.FileType;
import com.example.media.common.uploads.UploadStatus;
import com.example.users.repository.UserId;
import lombok.Builder;
import org.jetbrains.annotations.NotNull;

@Builder
public record InsertUpload(
        @NotNull String bucket,
        @NotNull String objectPath,
        @NotNull FileType fileType,
        @Builder.Default(UploadStatus.UPLOADING)
        @NotNull UploadStatus status,
        @NotNull UserId createdBy
) {
}
