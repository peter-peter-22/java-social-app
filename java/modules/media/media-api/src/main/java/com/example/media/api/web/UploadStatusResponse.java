package com.example.media.api.web;

import com.example.media.contract.upload.UploadAttemptStatus;
import org.jspecify.annotations.Nullable;

/**
 * The status of the key's active upload attempt. {@code version} is the read cache version of the
 * finalized upload; it is set only when {@code status} is {@code SUCCESS}, because the upload and
 * its version do not exist until the attempt finalizes.
 */
public record UploadStatusResponse(
        String uploadKey,
        UploadAttemptStatus status,
        @Nullable String version
) {
}
