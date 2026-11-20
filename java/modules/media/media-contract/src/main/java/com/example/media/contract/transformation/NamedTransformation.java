package com.example.media.contract.transformation;

import java.util.Objects;

/**
 * A named transformation at its current version.
 * The name is what clients request; the version is what results are produced from and pinned to.
 */
public record NamedTransformation(
        NamedTransformationId id,
        String name,
        TransformationSpec spec,
        TransformationVersionId currentVersionId
) {
    public NamedTransformation {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(name, "name");
        if (name.isBlank()) throw new IllegalArgumentException("name must not be blank");
        if (name.length() > 50)
            throw new IllegalArgumentException("name is longer than 50 characters: " + name);
        Objects.requireNonNull(spec, "spec");
        Objects.requireNonNull(currentVersionId, "currentVersionId");
    }
}