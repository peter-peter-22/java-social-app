package com.example.media.contract.port;

import com.example.media.contract.asset.*;

import java.util.Collection;
import java.util.Optional;

/**
 * The asset rows: registrations, metadata, and lifecycle status.
 * Assets are immutable once {@link AssetStatus#READY}; updates only move status and attach
 * metadata during the upload that produced the asset.
 */
public interface AssetStore {

    /**
     * Registers the asset in status {@link AssetStatus#UPLOADING}.
     * The id must be reserved first, because the asset id is the object prefix and the objects may
     * already be written when this is called.
     */
    Asset registerAsset(NewAsset insert);

    Optional<Asset> findAsset(AssetId id);

    /** Attaches the recorded image metadata of the asset. */
    void setImageMetadata(AssetId id, ImageMetadata metadata);

    /** Attaches the recorded video metadata of the asset. */
    void setVideoMetadata(AssetId id, VideoMetadata metadata);

    /** Moves all given assets to the same status. */
    void setStatus(Collection<AssetId> ids, AssetStatus status);

    /** Complete deletion of the asset registrations. Their objects must be deleted separately. */
    void deleteAssets(Collection<AssetId> ids);
}