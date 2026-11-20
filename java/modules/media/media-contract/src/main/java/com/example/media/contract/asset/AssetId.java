package com.example.media.contract.asset;

import java.util.Objects;
import java.util.UUID;

/**
 * The identity of a stored asset.
 * The id is also the prefix of the asset's objects, so ids are reserved before any object is written.
 */
public record AssetId(UUID value) {
    public AssetId {
        Objects.requireNonNull(value, "value");
    }

    /** Reserves a new asset id. */
    public static AssetId generate() {
        return new AssetId(UUID.randomUUID());
    }
}