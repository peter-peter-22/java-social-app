package com.example.media.contract.upload;

import java.util.Objects;
import java.util.UUID;

/** The identity of a finalized upload. */
public record UploadId(UUID value) {
    public UploadId {
        Objects.requireNonNull(value, "value");
    }
}