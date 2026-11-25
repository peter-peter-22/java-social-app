package com.example.media.contract.upload;

/** The upload permission has no remaining uses. */
public class UploadPermissionExhaustedException extends UploadPermissionDeniedException {
    public UploadPermissionExhaustedException(String message) {
        super(message);
    }
}
