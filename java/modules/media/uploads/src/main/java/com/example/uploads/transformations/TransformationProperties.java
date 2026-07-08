package com.example.uploads.transformations;

import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

import java.net.URI;

@ConfigurationProperties(prefix = "transformations.blocking")
@Validated
record TransformationProperties(
        @DefaultValue("http://localhost:9000") @NotNull URI imageTransformerUrl,
        @DefaultValue("http://localhost:9000") @NotNull URI videoTransformerUrl
) {
}
