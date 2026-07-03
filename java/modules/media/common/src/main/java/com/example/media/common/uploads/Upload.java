package com.example.media.common.uploads;

import com.example.users.repository.UserId;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;

public record Upload(
        @NotNull UploadId id,
        @NotNull UserId createdBy,
        @NotNull FileType fileType,
        @NotNull Instant createdAt,
        @NotNull UploadStatus status
) {
}
