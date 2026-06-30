package com.example.uploads.upload_repository;

import com.example.api.MediaType;
import com.example.users.repository.UserId;
import org.jetbrains.annotations.NotNull;

public record InsertUpload(
        @NotNull UserId createdBy,
        @NotNull String bucketName,
        @NotNull MediaType mediaType,
        @NotNull com.example.api.UploadStatus status,
        @NotNull String fileExtension
) {
}
