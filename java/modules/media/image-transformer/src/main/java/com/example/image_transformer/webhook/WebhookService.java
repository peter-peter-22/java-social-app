package com.example.image_transformer.webhook;

import com.example.media_api.transformations.task.UploadTransformationTask;
import com.example.media_api.transformations.webhook.WebhookCall;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WebhookService {
    private final WebhookApi webhookApi;

    public void handleCallback(@NotNull UploadTransformationTask transformation) {
        if (!transformation.isLazy()) return;
        var body = new WebhookCall(transformation.getName(), transformation.getOriginal());
        webhookApi.call(body);
        // TODO unit test
    }
}
