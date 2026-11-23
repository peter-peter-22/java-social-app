package com.example.media.contract.asset;

import com.example.media.contract.object.ObjectPrefix;

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

    /**
     * The prefix that owns every object of an asset placed here.
     * The asset id is the prefix, so keys of different assets cannot collide.
     */
    public ObjectPrefix prefixOf(AssetId assetId) {
        Objects.requireNonNull(assetId, "assetId");
        return new ObjectPrefix(homeRegion, objectBucket, assetId.value().toString());
    }
}