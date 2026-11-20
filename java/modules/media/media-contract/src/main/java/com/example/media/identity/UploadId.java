package com.example.media.identity;

import org.jspecify.annotations.NullMarked;

import java.util.Objects;
import java.util.UUID;

@NullMarked
public record UploadId(UUID value) {
    public UploadId {
        Objects.requireNonNull(value, "value");
    }
}
