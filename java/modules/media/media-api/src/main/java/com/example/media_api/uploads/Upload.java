package com.example.media_api.uploads;

import com.example.users.api.repository.UserId;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;

public record Upload(
        @NotNull UploadId id,
        @NotNull String objectPath,
        @NotNull String bucket,
        @NotNull UserId createdBy,
        @NotNull FileType fileType,
        @NotNull Instant createdAt,
        @NotNull UploadStatus status
) {
}
