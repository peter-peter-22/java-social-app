package com.example.uploads_api.stored_asset;

import java.util.UUID;

public interface StoredAssetRepository {
    StoredAsset insert(InsertStoredAsset insert);
    StoredAsset findById(UUID id);
    void setImageMetadata(UUID storedAssetId, ImageMetadata metadata);
    void setVideoMetadata(UUID storedAssetId, VideoMetadata metadata);
}
