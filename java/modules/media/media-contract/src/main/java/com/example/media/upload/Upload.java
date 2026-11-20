package com.example.media.upload;

import com.example.media.identity.AssetId;
import com.example.media.identity.UploadId;
import com.example.media.identity.UploadKey;
import org.jspecify.annotations.NullMarked;

import java.time.Instant;
import java.util.Objects;

/** A published original. The asset is loaded separately. */
@NullMarked
public record Upload(
        UploadKey uploadKey,
        UploadId id,
        Instant createdAt,
        AssetId assetId
) {
    public Upload {
        Objects.requireNonNull(uploadKey, "uploadKey");
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(createdAt, "createdAt");
        Objects.requireNonNull(assetId, "assetId");
    }
}
