package com.example.uploads.upload_variant_repository;

public class UploadVariantMissingUploadException extends RuntimeException {
    public UploadVariantMissingUploadException(String message) {
        super(message);
    }
}
