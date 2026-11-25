package com.example.media.contract.upload;

/** The upload permission expired before it was consumed. */
public class UploadPermissionExpiredException extends UploadPermissionDeniedException {
    public UploadPermissionExpiredException(String message) {
        super(message);
    }
}
