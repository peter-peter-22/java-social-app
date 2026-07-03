package com.example.media.uploads.transformations;

import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

import java.net.URI;

@ConfigurationProperties(prefix = "transformations.blocking")
@Validated
record BlockingTransformationProperties(
        @DefaultValue("http://localhost:9000") @NotNull URI endpoint
) {
}
