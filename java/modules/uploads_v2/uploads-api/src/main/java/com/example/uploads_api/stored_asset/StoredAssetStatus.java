package com.example.uploads_api.stored_asset;

public enum StoredAssetStatus {
    /**
     * Files can be uploaded to the object storage.
     * The status of the stored asset starts from UPLOADING, and it cannot reach this status again after changing.
     */
    UPLOADING,
    /**
     * The objects of the asset are getting permanent lifespan.
     * Some objects might have permanent lifespan now, the objects must be deleted before deleting the asset registration.
     * New uploads cannot be made.
     */
    FINALIZING,
    /**
     * The upload process that owns this asset was finished successfully.
     * This asset is no longer the target of cleanup.
     */
    READY
}
