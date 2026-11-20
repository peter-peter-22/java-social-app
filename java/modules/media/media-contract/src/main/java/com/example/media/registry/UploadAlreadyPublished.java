package com.example.media.registry;

/** The upload key already has a published upload, so a new attempt cannot take it. */
public final class UploadAlreadyPublished extends Exception {
    public UploadAlreadyPublished(String message) {
        super(message);
    }
}
