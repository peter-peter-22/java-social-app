package com.example.image_transformer.transformation;

import com.example.image_transformer.webhook.WebhookService;
import com.example.media_api.transformations.task.UploadTransformationTask;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TransformationService {
    private final WebhookService webhookService;
    private final TransformationOperationsHandler transformationOperationsHandler;

    public void applyTransformations(@NotNull UploadTransformationTask task) {
        transformationOperationsHandler.applyTransformationOperations(task);
        webhookService.handleCallback(task);
    }
    // TODO unit test
}
