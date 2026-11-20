package com.example.media.registry;

import com.example.media.asset.Asset;
import com.example.media.asset.ImageMetadata;
import com.example.media.identity.AssetId;
import org.jspecify.annotations.NullMarked;

import java.util.Collection;
import java.util.Optional;

/**
 * Stored-asset registry. The API writes these rows; the worker does not.
 * Insert the original before its object is written. A result id can be sent to the worker first
 * and inserted here once the worker reports its contents.
 */
@NullMarked
public interface AssetStore {
    /** Insert an uploading asset with the caller-chosen id. */
    Asset reserve(NewAsset asset);

    void attachImageMetadata(AssetId assetId, ImageMetadata metadata);

    void markFinalizing(Collection<AssetId> assetIds);

    void markReady(Collection<AssetId> assetIds);

    Optional<Asset> find(AssetId assetId);
}
