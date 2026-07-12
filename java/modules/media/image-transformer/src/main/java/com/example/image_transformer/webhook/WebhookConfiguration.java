package com.example.image_transformer.webhook;

import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.net.URI;

@ConfigurationProperties(prefix = "transformations.blocking")
@Validated
public record WebhookConfiguration(
        @NotNull
        URI webhookUrl
) {
}
