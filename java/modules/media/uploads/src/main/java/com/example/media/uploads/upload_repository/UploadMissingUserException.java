package com.example.media.uploads.upload_repository;

public class UploadMissingUserException extends RuntimeException {
    public UploadMissingUserException(String message) {
        super(message);
    }
}
