package com.example.uploads_service.transformations;

import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.net.URI;

@ConfigurationProperties(prefix = "transformations.blocking")
@Validated
record TransformationProperties(
        @NotNull URI imageTransformerUrl,
        @NotNull URI videoTransformerUrl
) {
}
