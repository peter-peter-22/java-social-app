package com.example.image_transformer.webhook;

import com.example.media_api.transformations.webhook.WebhookCall;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@EnableConfigurationProperties(WebhookConfiguration.class)
class WebhookApi {
    private final RestClient restClient;

    public WebhookApi(WebhookConfiguration configuration) {
        this.restClient = RestClient.create(configuration.webhookUrl());
    }

    @Retryable
    public void call(@NotNull WebhookCall body) {
        restClient.post()
                .uri("/")
                .body(body)
                .retrieve()
                .toBodilessEntity();
    }
}
