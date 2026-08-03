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

    // TODO CLEAN: this seems like there is a better way
    public void handleWebhookCall(@NonNull TransformationTask task, @NonNull TransformationTaskGroup group) {
        if (!task.async()) return;
        // TODO investigate if the upload id belongs here
        var body = WebhookCall.builder()
                .uploadId(group.uploadId())
                .transformationName(task.name())
                .build();
        webhookApi.call(body);
    }
}
