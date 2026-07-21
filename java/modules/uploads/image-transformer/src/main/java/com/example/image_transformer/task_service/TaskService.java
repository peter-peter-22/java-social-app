package com.example.image_transformer.task_service;

import com.example.image_transformer.operations.ImageTransformationService;
import com.example.image_transformer.storage.FileStreamStorage;
import com.example.image_transformer.stream_processing.FileStreamProcessingManager;
import com.example.image_transformer.task.ImageTransformationTask;
import com.example.image_transformer.task.ImageTransformationTaskGroup;
import com.example.image_transformer.webhook.WebhookService;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TaskService {
    private final WebhookService webhookService;
    private final ImageTransformationService transformationService;
    private final FileStreamProcessingManager fileStreamProcessingManager;
    private final FileStreamStorage storage;

    public void processTasks(@NonNull ImageTransformationTaskGroup group) {
        var source = fileStreamProcessingManager.readAllBytes(() -> storage.read(group.inputObject()));
        group.tasks().forEach(task -> processTask(task, source));
    }

    private void processTask(@NonNull ImageTransformationTask task, byte[] source) {
        fileStreamProcessingManager.process(
                source,
                stream -> transformationService.transformFile(stream, task.operations()),
                stream -> storage.write(stream, task.outputObject())
        );
        webhookService.handleCallback(task);
    }
}
