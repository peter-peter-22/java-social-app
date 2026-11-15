package com.example.upload_registry.upload_service;

import com.example.uploads_api.stored_asset.StoredAsset;

import java.util.UUID;

public record StartedUploadAttempt(
        String uploadKey,
        UUID attemptId,
        StoredAsset originalUpload
) {
}
