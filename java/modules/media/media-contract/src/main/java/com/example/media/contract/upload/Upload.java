package com.example.media.contract.upload;

import com.example.media.contract.asset.AssetId;

import java.time.Instant;
import java.util.Objects;

/**
 * A finalized upload: the durable result of a successful attempt.
 * References its asset by id instead of embedding it; joins are a query-side concern.
 */
public record Upload(
        UploadId id,
        UploadKey key,
        AssetId assetId,
        UploadVisibility visibility,
        Instant createdAt
) {
    public Upload {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(assetId, "assetId");
        Objects.requireNonNull(visibility, "visibility");
        Objects.requireNonNull(createdAt, "createdAt");
    }

    /**
     * The read cache version of this upload: its creation instant as decimal epoch milliseconds.
     * A replaced upload is a new row with a new {@code createdAt}, so its version changes and the
     * old read URLs and download tokens stop matching. The version is part of a download token's
     * signature, so a token minted for one version cannot read another.
     */
    public String version() {
        return Long.toString(createdAt.toEpochMilli());
    }
}