package com.example.media.api.web;

import com.example.media.contract.transformation.TransformationSpec;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.jspecify.annotations.Nullable;

/**
 * The admin body that defines or updates a named transformation. The spec travels in the shared
 * media JSON shape (the polymorphic {@code type} property of {@code media-json}), so the browser and
 * admin clients write exactly the shape the registry persists. Unknown fields are ignored, so the
 * client shape can grow without breaking this DTO.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AdminTransformationRequest(
        @Nullable String name,
        @Nullable TransformationSpec spec
) {
}
