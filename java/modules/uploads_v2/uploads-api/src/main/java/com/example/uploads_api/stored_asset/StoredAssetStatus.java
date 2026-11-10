package com.example.uploads_api.stored_asset;

public enum StoredAssetStatus {
    /** Files can be uploaded to the object storage.
     * The status of the stored asset starts from UPLOADING, and it cannot reach this status again after changing. */
    UPLOADING,
    /** The objects are uploaded. The asset is waiting for the owner process to finish. */
    UPLOADED,
    /** The owner process is finished. The object can be used. */
    LINKED,
    /** The asset is marked for deletion while it was uploading.
     * The object storage upload periodically checks for this status */
    DELETING
}
