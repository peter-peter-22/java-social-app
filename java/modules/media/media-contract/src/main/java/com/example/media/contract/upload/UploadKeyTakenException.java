package com.example.media.contract.upload;

/** The upload key is already taken by another upload. */
public class UploadKeyTakenException extends RuntimeException {
    public UploadKeyTakenException(String message) {
        super(message);
    }
}