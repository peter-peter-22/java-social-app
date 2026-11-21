package com.example.media.api.web;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * The JSON body part of an image upload request. Unknown fields are ignored, so the client shape
 * can grow the async and permission fields later without breaking this DTO.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ImageUploadRequest(
        @Nullable String uploadKey,
        @Nullable String extension,
        @Nullable String desiredContentType,
        @Nullable Integer desiredBytes,
        @Nullable List<String> eagerTransformations,
        @Nullable String incomingTransformation
) {
}