package com.example.uploads_service.processing;

import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.net.URI;

@ConfigurationProperties(prefix = "uploads.processing")
public record UploadProcessingProperties(
        @NotNull URI imageProcessingUrl,
        @NotNull URI videoProcessingUrl
) {
}
