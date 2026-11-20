package com.example.media.contract.upload;

import com.example.media.contract.asset.AssetId;

import java.util.Objects;

/**
 * The data needed to register the final upload of an attempt.
 * Registration is atomic and cannot overwrite: the key must be free and the attempt must still
 * be the process's active one.
 */
public record NewUpload(UploadKey key, AttemptId attemptId, AssetId assetId) {
    public NewUpload {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(attemptId, "attemptId");
        Objects.requireNonNull(assetId, "assetId");
    }
}