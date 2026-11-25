package com.example.media.api.web;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.jspecify.annotations.Nullable;

/**
 * The admin body that issues one download permission for a finalized upload. {@code uploadKey} names
 * the upload to authorize; {@code expiresInSeconds} optionally overrides the configured default
 * lifetime and is clamped to the configured maximum. Unknown fields are ignored, so the client shape
 * can grow without breaking this DTO.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AdminDownloadPermissionRequest(
        @Nullable String uploadKey,
        @Nullable Long expiresInSeconds
) {
}