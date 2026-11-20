package com.example.media.contract.asset;

/**
 * The lifecycle state of an asset.
 * The names are the values stored in the registry's status column; do not rename them.
 */
public enum AssetStatus {
    /**
     * Files can be uploaded to the object store.
     * The status starts here and cannot be reached again after leaving it.
     */
    UPLOADING,
    /**
     * The objects of the asset are getting permanent lifespan.
     * Some objects might already be permanent; the objects must be deleted before deleting the
     * asset registration. New uploads cannot be made.
     */
    FINALIZING,
    /**
     * The upload process that owns this asset finished successfully.
     * This asset is no longer the target of cleanup.
     */
    READY,
    /** Marked for immediate deletion. */
    DELETE
}