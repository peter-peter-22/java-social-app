package com.example.media.contract.worker;

import com.example.media.contract.object.ObjectRef;
import com.example.media.contract.upload.AttemptId;

import java.util.List;
import java.util.Objects;

/**
 * What a worker must do for one attempt.
 * Carries a source object location, the reserved output prefixes, and the resolved operations.
 * It deliberately carries no registry entities: the worker does not know the database.
 */
public record ImageTransformCommand(AttemptId attemptId, ObjectRef source, List<PlannedImageOutput> outputs) {
    public ImageTransformCommand {
        Objects.requireNonNull(attemptId, "attemptId");
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(outputs, "outputs");
        outputs = List.copyOf(outputs);
        if (outputs.isEmpty())
            throw new IllegalArgumentException("a transform command must plan at least one output");
    }
}