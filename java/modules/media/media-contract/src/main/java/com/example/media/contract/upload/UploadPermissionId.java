package com.example.media.contract.upload;

import java.util.Objects;
import java.util.UUID;

/** The identity of one upload permission: the database row behind an issued upload token. */
public record UploadPermissionId(UUID value) {
    public UploadPermissionId {
        Objects.requireNonNull(value, "value");
    }
}
