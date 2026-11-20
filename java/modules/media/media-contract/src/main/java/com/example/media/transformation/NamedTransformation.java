package com.example.media.transformation;

import com.example.media.identity.NamedTransformationId;
import com.example.media.identity.TransformationVersionId;
import org.jspecify.annotations.NullMarked;

import java.util.Objects;

/**
 * A named image transformation and the version a variant must reference.
 * The name length matches the current column.
 */
@NullMarked
public record NamedTransformation(
        NamedTransformationId id,
        String name,
        TransformationVersionId currentVersionId,
        ImageTransformation operations
) {
    public static final int MAX_NAME_LENGTH = 50;

    public NamedTransformation {
        Objects.requireNonNull(id, "id");
        if (name == null || name.isBlank() || name.length() > MAX_NAME_LENGTH) {
            throw new IllegalArgumentException("Transformation name must be 1 to " + MAX_NAME_LENGTH + " characters");
        }
        Objects.requireNonNull(currentVersionId, "currentVersionId");
        Objects.requireNonNull(operations, "operations");
    }
}
