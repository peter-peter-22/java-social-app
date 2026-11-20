package com.example.media.contract.upload;

import com.example.media.contract.asset.AssetId;

import java.time.Instant;
import java.util.Objects;

/**
 * A finalized upload: the durable result of a successful attempt.
 * References its asset by id instead of embedding it; joins are a query-side concern.
 */
public record Upload(UploadId id, UploadKey key, AssetId assetId, Instant createdAt) {
    public Upload {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(assetId, "assetId");
        Objects.requireNonNull(createdAt, "createdAt");
    }
}