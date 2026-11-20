package com.example.media.contract.transformation;

import java.util.Objects;
import java.util.UUID;

/** The identity of a named transformation. Stable across its versions. */
public record NamedTransformationId(UUID value) {
    public NamedTransformationId {
        Objects.requireNonNull(value, "value");
    }
}