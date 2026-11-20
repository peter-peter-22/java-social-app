package com.example.media.upload;

import com.example.media.identity.AssetId;
import com.example.media.identity.AttemptId;
import com.example.media.identity.UploadKey;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

@NullMarked
public record UploadAttempt(
        AttemptId attemptId,
        UploadKey uploadKey,
        List<String> eagerTransformationNames,
        @Nullable String incomingTransformationName,
        AssetId originalAssetId,
        Instant createdAt,
        AttemptStatus status,
        @Nullable Instant finishedAt
) {
    public UploadAttempt {
        Objects.requireNonNull(attemptId, "attemptId");
        Objects.requireNonNull(uploadKey, "uploadKey");
        Objects.requireNonNull(eagerTransformationNames, "eagerTransformationNames");
        eagerTransformationNames = List.copyOf(eagerTransformationNames);
        for (String name : eagerTransformationNames) {
            if (name == null || name.isBlank()) {
                throw new IllegalArgumentException("Eager transformation names must not be blank");
            }
        }
        if (incomingTransformationName != null && incomingTransformationName.isBlank()) {
            throw new IllegalArgumentException("Incoming transformation name must not be blank");
        }
        Objects.requireNonNull(originalAssetId, "originalAssetId");
        Objects.requireNonNull(createdAt, "createdAt");
        Objects.requireNonNull(status, "status");
        boolean finished = status == AttemptStatus.SUCCESS || status == AttemptStatus.FAILED;
        if (finished && finishedAt == null) {
            throw new IllegalArgumentException("A finished attempt must have a finish time");
        }
        if (!finished && finishedAt != null) {
            throw new IllegalArgumentException("An open attempt must not have a finish time");
        }
    }
}
