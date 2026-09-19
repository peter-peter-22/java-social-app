package com.example.transformer_contracts.webhook;

import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.net.URI;

@ConfigurationProperties(prefix = "transformations")
@Validated
public record WebhookProperties(
        @NotNull
        URI webhookUrl
) {
}
