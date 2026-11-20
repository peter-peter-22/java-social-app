package com.example.media.contract.object;

import java.util.Objects;

/**
 * The location of a single object in the object store.
 * Plain strings on purpose: storage vendor types must not leak past the adapter.
 */
public record ObjectRef(String region, String bucket, String key) {
    public ObjectRef {
        Objects.requireNonNull(region, "region");
        Objects.requireNonNull(bucket, "bucket");
        Objects.requireNonNull(key, "key");
    }
}