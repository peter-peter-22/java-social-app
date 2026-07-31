package com.example.transformer_contracts.webhook;


import com.example.uploads_api.transformations.tasks.TransformationTask;
import com.example.uploads_api.transformations.tasks.TransformationTaskGroup;
import com.example.uploads_api.transformations.webhook.WebhookCall;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WebhookService {
    private final WebhookApi webhookApi;

    public void handleWebhookCalls(@NonNull TransformationTaskGroup tasks) {
        var lazyTransformationNames = tasks.tasks().stream()
                .filter(TransformationTask::lazy)
                .map(TransformationTask::name)
                .toList();
        if (lazyTransformationNames.isEmpty()) return;
        // TODO investigate if the upload id belongs here
        var body = WebhookCall.builder()
                .uploadId(tasks.uploadId())
                .transformationNames(lazyTransformationNames)
                .build();
        webhookApi.call(body);
    }
}
