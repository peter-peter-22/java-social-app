package com.example.media.api.web;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * The JSON body part of an image upload request. Unknown fields are ignored, so the client shape
 * can grow without breaking this DTO. {@code async} requests queued eager and incoming
 * transformations.
 *
 * <p>{@code permission} is the raw upload token issued by the admin endpoint. When it is present,
 * the constraints stored with it are authoritative: the upload must match them, and it takes its
 * key and visibility from them. A request without a permission is public, and it is accepted only
 * while the deployment does not require a permission.</p>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ImageUploadRequest(
        @Nullable String permission,
        @Nullable String uploadKey,
        @Nullable String extension,
        @Nullable String desiredContentType,
        @Nullable Integer desiredBytes,
        @Nullable List<String> eagerTransformations,
        @Nullable String incomingTransformation,
        @Nullable Boolean async
) {
}