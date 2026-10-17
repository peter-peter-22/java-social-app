package com.example.uploads_api.stored_asset;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

@NullMarked
public interface StoredAssetRepository {
    UUID insert(InsertStoredAsset insert);
    @Nullable StoredAsset findById(UUID id);
    void delete(UUID id);
    void setImageMetadata(UUID storedAssetId, ImageMetadata metadata);
    void setVideoMetadata(UUID storedAssetId, VideoMetadata metadata);
}
