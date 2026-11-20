package com.example.media.contract.upload;

import com.example.media.contract.asset.AssetId;
import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * One try at producing the final upload of a process.
 * Attempt records are kept after failure; they are small and interfere with nothing.
 */
public record UploadAttempt(
        AttemptId id,
        UploadKey key,
        AssetId originalAssetId,
        List<String> eagerTransformationNames,
        @Nullable String incomingTransformationName,
        UploadAttemptStatus status,
        Instant createdAt,
        @Nullable Instant finishedAt
) {
    public UploadAttempt {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(originalAssetId, "originalAssetId");
        Objects.requireNonNull(eagerTransformationNames, "eagerTransformationNames");
        eagerTransformationNames = List.copyOf(eagerTransformationNames);
        Objects.requireNonNull(status, "status");
        Objects.requireNonNull(createdAt, "createdAt");
    }
}