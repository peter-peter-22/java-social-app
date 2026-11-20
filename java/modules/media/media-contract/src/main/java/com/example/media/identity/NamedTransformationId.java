package com.example.media.identity;

import org.jspecify.annotations.NullMarked;

import java.util.Objects;
import java.util.UUID;

@NullMarked
public record NamedTransformationId(UUID value) {
    public NamedTransformationId {
        Objects.requireNonNull(value, "value");
    }
}
