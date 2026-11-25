package com.example.media.contract.upload;

/**
 * The state of an upload permission.
 * The names are the values stored in the registry's status column; do not rename them.
 * Expiry is not a status: it is derived from the permission's expiry instant.
 */
public enum UploadPermissionStatus {
    /** The permission can still be consumed: it is not expired and has remaining uses. */
    ACTIVE,
    /** The permission consumed its last use. */
    EXHAUSTED,
    /** The permission was revoked before its uses ran out, for example by a failed upload attempt. */
    REVOKED
}
