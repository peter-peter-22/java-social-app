package com.example.media.api.upload;

/**
 * The requested upload does not exist: no upload is finalized under the key, or the attempt of a
 * completion is unknown. Delivered as 404.
 */
public class UploadNotFoundException extends RuntimeException {
    public UploadNotFoundException(String message) {
        super(message);
    }
}