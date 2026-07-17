package com.example.uploads_persistence.upload_repository;

public class UploadMissingUserException extends RuntimeException {
    public UploadMissingUserException(String message) {
        super(message);
    }
}
