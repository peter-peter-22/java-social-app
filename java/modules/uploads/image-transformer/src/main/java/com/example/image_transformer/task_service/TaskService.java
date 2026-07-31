package com.example.image_transformer.task_service;

import com.example.image_transformer.operations.ImageTransformationService;
import com.example.transformer_contracts.storage.FileStreamStorage;
import com.example.transformer_contracts.stream_processing.FileStreamProcessingManager;
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
    private final FileStreamStorage storage;

    public void processTasks(@NonNull ImageTransformationTaskGroup group) {
        var source = FileStreamProcessingManager.readAllBytes(() -> storage.read(group.inputObject()));
        group.tasks().forEach(task -> processTask(task, source));
        webhookService.handleWebhookCalls(group);
    }

    // OPTIMIZE: should this be parallel?
    // TODO: CLEAN: this could be extracted to the contracts module
    private void processTask(ImageTransformationTaskGroup.@NonNull ImageTask task, byte[] source) {
        FileStreamProcessingManager.process(
                source,
                stream -> transformationService.transformFile(stream, task.operations()),
                stream -> storage.write(stream, task.outputObject())
        );
    }
}
