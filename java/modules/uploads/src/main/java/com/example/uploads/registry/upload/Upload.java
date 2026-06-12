package com.example.uploads.registry.upload;

import com.example.uploads.registry.UploadId;
import com.example.users.UserId;
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
