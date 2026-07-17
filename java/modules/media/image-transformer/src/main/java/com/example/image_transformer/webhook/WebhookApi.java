package com.example.image_transformer.webhook;

import com.example.uploads_api.transformations.webhook.WebhookCall;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;

import java.util.concurrent.TimeUnit;

@Component
@EnableConfigurationProperties(WebhookProperties.class)
class WebhookApi {
    private final RestClient restClient;

    public WebhookApi(WebhookProperties configuration) {
        this.restClient = RestClient.create(configuration.webhookUrl());
    }

    @Retryable(
            value = HttpServerErrorException.InternalServerError.class,
            maxRetries = 2,
            delay = 1,
            maxDelay = 1,
            timeUnit = TimeUnit.SECONDS
    )
    public void call(@NotNull WebhookCall body) {
        restClient.post()
                .uri("/")
                .body(body)
                .retrieve()
                .toBodilessEntity();
    }
}
