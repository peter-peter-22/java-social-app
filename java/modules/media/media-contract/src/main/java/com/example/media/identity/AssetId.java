package com.example.media.identity;

import org.jspecify.annotations.NullMarked;

import java.util.Objects;
import java.util.UUID;

/** Caller-chosen id. It is also the object prefix, so two assets cannot share a key. */
@NullMarked
public record AssetId(UUID value) {
    public AssetId {
        Objects.requireNonNull(value, "value");
    }
}
