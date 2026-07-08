package com.example.image_transformer.webhook;

import com.example.media_api.transformations.api.UploadTransformationDTO;
import com.example.media_api.transformations.api.WebhookDTO;
import org.jetbrains.annotations.NotNull;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class WebhookService {
    private final RestClient restClient = RestClient.create();

    /**
     * Call the callback URL if exists.
     */
    public void handleCallback(@NotNull UploadTransformationDTO transformation) {
        if (transformation.webhookUrl() == null) return;
        var body = new WebhookDTO(transformation.name(), transformation.original());
        call(transformation.webhookUrl(), body);
    }

    @Retryable
    private void call(@NotNull String url, @NotNull WebhookDTO body) {
        restClient.post()
                .uri(url)
                .body(body)
                .retrieve()
                .toBodilessEntity();
    }
}
