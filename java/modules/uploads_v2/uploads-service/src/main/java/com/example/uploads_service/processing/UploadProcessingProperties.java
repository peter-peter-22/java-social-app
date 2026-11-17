package com.example.uploads_service.processing;

import jakarta.validation.constraints.NotNull;
import org.jspecify.annotations.NullMarked;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.net.URI;

@ConfigurationProperties(prefix = "uploads.processing")
@Validated
@NullMarked
public record UploadProcessingProperties(
        @NotNull URI imageProcessingUrl,
        @NotNull URI videoProcessingUrl
) {
}
