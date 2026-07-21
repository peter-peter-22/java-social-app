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

import java.io.ByteArrayInputStream;

@Service
@RequiredArgsConstructor
public class TaskService {
    private final WebhookService webhookService;
    private final ImageTransformationService transformationService;
    private final FileStreamProcessingManager fileStreamProcessingManager;
    private final FileStreamStorage storage;

    public void processTasks(@NonNull ImageTransformationTaskGroup group) {
        try (var inputStream = storage.read(group.inputObject())) {
            // TODO update FileStreamProcessingManager
            var source = inputStream.readAllBytes();
            group.tasks().forEach(task -> processTask(task, source));
        } catch (Exception e) {
            throw new RuntimeException("Failed to read source image for transformations", e);
        }
    }

    private void processTask(@NonNull ImageTransformationTask task, byte[] source) {
        fileStreamProcessingManager.process(
                () -> new ByteArrayInputStream(source),
                stream-> transformationService.transformFile(stream,task.operations()),
                stream-> storage.write(stream,task.outputObject())
        );
        webhookService.handleCallback(task);
    }
    // TODO unit test
}
