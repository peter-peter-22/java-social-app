package com.example.media.contract.upload;

/**
 * The presented upload permission cannot be used.
 * Thrown when the token is unknown or its permission was revoked. Expired and exhausted
 * permissions have their own subclasses.
 */
public class UploadPermissionDeniedException extends RuntimeException {
    public UploadPermissionDeniedException(String message) {
        super(message);
    }
}
