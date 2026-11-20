package com.example.media.contract.transformation;

import java.util.Objects;
import java.util.UUID;

/**
 * The identity of one recorded version of a named transformation's spec.
 * Variants pin a version, so editing a named transformation never changes existing variants.
 */
public record TransformationVersionId(UUID value) {
    public TransformationVersionId {
        Objects.requireNonNull(value, "value");
    }
}