package com.example.media.worker;

import com.example.media.identity.AttemptId;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Objects;

/**
 * Successful worker output. A failure is an exception from the blocking adapter, not a result.
 * This type does not tell the caller to finalize; the API decides that after checking the objects.
 */
@NullMarked
public record ImageWorkResult(
        AttemptId attemptId,
        @Nullable StoredObjectResult incoming,
        List<StoredObjectResult> eager
) {
    public ImageWorkResult {
        Objects.requireNonNull(attemptId, "attemptId");
        Objects.requireNonNull(eager, "eager");
        eager = List.copyOf(eager);
    }
}
