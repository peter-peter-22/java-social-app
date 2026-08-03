package com.example.image_transformer.task_service;

import com.example.image_transformer.operations.ImageTransformationService;
import com.example.transformer_contracts.webhook.WebhookService;
import com.example.uploads_api.transformations.tasks.ImageTransformationTaskGroup;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TaskService {
    private final WebhookService webhookService;
    private final ImageTransformationService transformationService;

    public void processTask(@NonNull ImageTransformationTaskGroup group) {
        // define download path and task directory with path manager
        // download original to disk
        // process task
        // upload all outputs, use variant key manager

        // requirements: create path manage in transformer contracts and object key manage in upload contracts or upload persistence
    }
}
