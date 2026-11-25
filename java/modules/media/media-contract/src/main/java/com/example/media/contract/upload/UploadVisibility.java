package com.example.media.contract.upload;

/**
 * Who may read an upload and its variants.
 * The names are the values stored in the registry's visibility columns; do not rename them.
 */
public enum UploadVisibility {
    /** Anyone who knows the upload key may read the upload. New uploads default to this. */
    PUBLIC,
    /** A read requires a download credential bound to the upload key, its version, and an expiry. */
    PRIVATE
}
