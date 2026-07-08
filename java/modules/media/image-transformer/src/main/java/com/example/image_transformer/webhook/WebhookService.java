package com.example.image_transformer.webhook;

import com.example.media_api.transformations.api.UploadTransformationDTO;
import com.example.media_api.transformations.api.WebhookDTO;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
@RequiredArgsConstructor
public class WebhookService {
    private final WebhookApi webhookApi;

    public void handleCallback(@NotNull UploadTransformationDTO transformation) {
        if (!transformation.webhook()) return;
        var body = new WebhookDTO(transformation.name(), transformation.original());
        webhookApi.call(body);
        // TODO unit test
    }
}
