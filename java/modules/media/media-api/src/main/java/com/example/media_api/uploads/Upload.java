package com.example.media_api.uploads;

import com.example.users_api.repository.UserId;
import lombok.Builder;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;

@Builder
public record Upload(
        @NotNull UploadId id,
        @NotNull ObjectLocation objectLocation,
        @NotNull UserId createdBy,
        @NotNull FileType fileType,
        @NotNull Instant createdAt,
        @NotNull UploadStatus status
) {
}
