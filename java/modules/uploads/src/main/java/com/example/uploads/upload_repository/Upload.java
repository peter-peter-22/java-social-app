package com.example.uploads.upload_repository;

import com.example.users.repository.UserId;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;

public record Upload(
        @NotNull UploadId id,
        @NotNull UserId createdBy,
        @NotNull String bucketName,
        @NotNull MediaType mediaType,
        @NotNull Instant createdAt,
        @NotNull String transformationGroup,
        @NotNull Integer transformationVersion,
        @NotNull UploadStatus status,
        @NotNull String fileExtension
) {
}
