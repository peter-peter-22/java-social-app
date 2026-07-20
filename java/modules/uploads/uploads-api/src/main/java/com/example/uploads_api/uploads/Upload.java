package com.example.uploads_api.uploads;

import com.example.users_api.repository.UserId;
import lombok.Builder;
import org.jspecify.annotations.NonNull;

import java.time.Instant;

@Builder
public record Upload(
        @NonNull UploadId id,
        @NonNull ObjectLocation objectLocation,
        @NonNull UserId createdBy,
        @NonNull FileType fileType,
        @NonNull Instant createdAt,
        @NonNull UploadStatus status
) {
}
