package com.example.image_transformer.task_service;

import com.example.image_transformer.operations.ImageTransformationService;
import com.example.image_transformer.task.ImageTransformationTaskGroup;
import com.example.transformer_contracts.storage.FileStreamStorage;
import com.example.transformer_contracts.stream_processing.FileStreamProcessingManager;
import com.example.transformer_contracts.webhook.WebhookService;
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
    }

    // OPTIMIZE: should this be parallel?
    private void processTask(ImageTransformationTaskGroup.@NonNull Task task, byte[] source) {
        FileStreamProcessingManager.process(
                source,
                stream -> transformationService.transformFile(stream, task.operations()),
                stream -> storage.write(stream, task.outputObject())
        );
        webhookService.handleCallback(task);
    }
}
