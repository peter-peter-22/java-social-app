package com.example.media.asset;

/**
 * An asset starts at {@link #UPLOADING} and never returns to it.
 * {@link #FINALIZING} means some objects may already be permanent, so cleanup must delete the objects
 * before deleting the row. {@link #READY} is no longer a cleanup target.
 */
public enum AssetStatus {
    UPLOADING,
    FINALIZING,
    READY,
    DELETE
}
