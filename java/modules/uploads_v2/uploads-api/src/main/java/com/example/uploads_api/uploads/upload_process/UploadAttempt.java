package com.example.uploads_api.uploads.upload_process;

import lombok.Builder;
import lombok.Getter;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@NullMarked
@Getter
@Builder
public final class UploadAttempt {
    private final UUID attemptId;
    private final String uploadKey;
    private final @Nullable List<String> eagerTransformationNames;
    private final @Nullable String incomingTransformationName;
    /** The original file from the upload request without incoming transformation. */
    private final UUID originalAssetId;
    private final Instant createdAt;
    /** Initial status: PENDING */
    private final UploadAttemptStatus status;
    private final @Nullable Instant finishedAt;
}
