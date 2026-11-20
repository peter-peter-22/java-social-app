package com.example.media.identity;

import org.jspecify.annotations.NullMarked;

/**
 * Public id of an upload process and, once published, of the upload.
 * The length matches the current {@code upload_key} column.
 */
@NullMarked
public record UploadKey(String value) {
    public static final int MAX_LENGTH = 100;

    public UploadKey {
        if (value == null || value.isBlank() || value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("Upload key must be 1 to " + MAX_LENGTH + " characters");
        }
    }
}
