package com.example.uploads.upload_repository;

import com.example.users.repository.UserId;
import org.jetbrains.annotations.NotNull;

public record InsertUpload(
        @NotNull UserId createdBy,
        @NotNull String bucketName,
        @NotNull MediaType mediaType,
        @NotNull String transformationGroup,
        @NotNull Integer transformationVersion,
        @NotNull UploadStatus status,
        @NotNull String fileExtension
) {
}
