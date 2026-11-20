package com.example.media.contract.upload;

import java.util.Objects;

/**
 * The client-chosen key of an upload. It identifies the upload process and its final upload.
 * The length limit follows the registry column; do not raise it without a schema change.
 */
public record UploadKey(String value) {
    public UploadKey {
        Objects.requireNonNull(value, "value");
        if (value.isBlank())
            throw new IllegalArgumentException("upload key must not be blank");
        if (value.length() > 100)
            throw new IllegalArgumentException("upload key is longer than 100 characters: " + value);
    }
}