package com.example.image_transformer.webhook;


import com.example.image_transformer.task.ImageTransformationTask;
import com.example.media_api.transformations.webhook.WebhookCall;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WebhookService {
    private final WebhookApi webhookApi;

    public void handleCallback(@NotNull ImageTransformationTask task) {
        if (!task.lazy()) return;
        // TODO investigate if the upload id belongs here
        var body = new WebhookCall(task.uploadId(), task.name());
        webhookApi.call(body);
        // TODO unit test
    }
}
