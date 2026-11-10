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
    /** The assets of the eager and incoming transformations are tracked here.
     * When the attempt fails and a next attempt starts, these assets and their files are deleted, only
     * the original is preserved.
     * When an attempt finishes successfully, these assets are preserved. */
    private final @Nullable List<UUID> createdAssetIds;
    private final Instant createdAt;
    /** Initial status: PENDING */
    private final UploadAttemptStatus result;
    private final @Nullable Instant finishedAt;
    // TODO add some unique id for the signatures to make it possible to track how much times the given signature was used before and prevent stressing the server
}
