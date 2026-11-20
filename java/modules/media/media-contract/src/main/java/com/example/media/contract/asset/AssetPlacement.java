package com.example.media.contract.asset;

import java.util.Objects;

/**
 * Where an asset's objects live: the home region and the bucket recorded for the asset.
 * The values come from the object store adapter's configuration at registration time.
 */
public record AssetPlacement(String homeRegion, String objectBucket) {
    public AssetPlacement {
        Objects.requireNonNull(homeRegion, "homeRegion");
        Objects.requireNonNull(objectBucket, "objectBucket");
    }
}