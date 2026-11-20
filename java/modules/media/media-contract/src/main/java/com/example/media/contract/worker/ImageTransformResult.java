package com.example.media.contract.worker;

import com.example.media.contract.asset.ImageMetadata;
import com.example.media.contract.upload.AttemptId;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Objects;

/**
 * What a worker produced for one attempt.
 * Reports metadata and the object list only: the API registers rows, changes lifetimes, and
 * finalizes the attempt.
 */
public record ImageTransformResult(
        AttemptId attemptId,
        List<CompletedImageOutput> outputs,
        @Nullable ImageMetadata sourceMetadata
) {
    public ImageTransformResult {
        Objects.requireNonNull(attemptId, "attemptId");
        Objects.requireNonNull(outputs, "outputs");
        outputs = List.copyOf(outputs);
    }
}