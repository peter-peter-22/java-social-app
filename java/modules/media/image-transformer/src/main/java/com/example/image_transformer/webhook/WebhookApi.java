package com.example.image_transformer.webhook;

import com.example.media_api.transformations.api.WebhookDTO;
import org.jetbrains.annotations.NotNull;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
class WebhookApi {
    // TODO read url from config
    private final RestClient restClient = RestClient.create("/api/media/callback");

    @Retryable
    public void call(@NotNull WebhookDTO body) {
        restClient.post()
                .uri("/mark_as_ready")
                .body(body)
                .retrieve()
                .toBodilessEntity();
    }
}
