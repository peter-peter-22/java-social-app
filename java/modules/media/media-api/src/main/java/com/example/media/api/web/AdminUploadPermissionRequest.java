package com.example.media.api.web;

import com.example.media.contract.upload.UploadVisibility;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * The admin body that issues one upload permission. The values stored with the permission become
 * authoritative for the upload that consumes it; a client may only start an upload that matches
 * them. The upload key, the TTL, the use budget, and the visibility are optional and take the
 * service's defaults. Unknown fields are ignored, so the client shape can grow without breaking
 * this DTO.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AdminUploadPermissionRequest(
        @Nullable String uploadKey,
        @Nullable String extension,
        @Nullable String contentType,
        @Nullable Long maxBytes,
        @Nullable List<String> eagerTransformations,
        @Nullable String incomingTransformation,
        @Nullable UploadVisibility visibility,
        @Nullable Long expiresInSeconds,
        @Nullable Integer maxUses
) {
}
