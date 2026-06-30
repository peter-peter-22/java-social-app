package com.example.uploads;

import com.example.users.repository.UserId;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;

public record Upload(
        @NotNull UploadId id,
        @NotNull UserId createdBy,
        @NotNull String bucketName,
        @NotNull MediaType mediaType,
        @NotNull Instant createdAt,
        @NotNull UploadStatus status
) {
}
