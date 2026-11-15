package com.example.uploads_api.stored_asset;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.UUID;

@NullMarked
public interface StoredAssetRepository {
    StoredAsset insert(InsertStoredAsset insert);

    @Nullable StoredAsset findById(UUID id);

    /** Insert an image metadata that belongs to the given stored asset. */
    void setImageMetadata(UUID storedAssetId, ImageMetadata metadata);

    /** Insert a video metadata that belongs to the given stored asset. */
    void setVideoMetadata(UUID storedAssetId, VideoMetadata metadata);

    void setStatusAll(Collection<UUID> ids,  StoredAssetStatus status);

    /** Complete deletion. */
    void deleteAll(Collection<UUID> ids);
}
