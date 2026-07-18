package com.example.uploads_api.transformations.upload_repository;

public class UploadMissingUserException extends RuntimeException {
    public UploadMissingUserException(String message) {
        super(message);
    }
}
