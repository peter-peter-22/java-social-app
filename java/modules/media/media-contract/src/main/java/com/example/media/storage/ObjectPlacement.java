package com.example.media.storage;

import org.jspecify.annotations.NullMarked;

/** Region and bucket the object store assigns to new assets. */
@NullMarked
public record ObjectPlacement(String region, String bucket) {
    public ObjectPlacement {
        if (region == null || region.isBlank()) {
            throw new IllegalArgumentException("Region must not be blank");
        }
        if (bucket == null || bucket.isBlank()) {
            throw new IllegalArgumentException("Bucket must not be blank");
        }
    }
}
