package com.example.image_transformer.webhook;


import com.example.image_transformer.task.ImageTransformationTask;
import com.example.uploads_api.transformations.webhook.WebhookCall;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WebhookService {
    private final WebhookApi webhookApi;

    public void handleCallback(@NonNull ImageTransformationTask task) {
        if (!task.lazy()) return;
        // TODO investigate if the upload id belongs here
        var body = new WebhookCall(task.uploadId(), task.name());
        webhookApi.call(body);
        // TODO unit test
    }
}
