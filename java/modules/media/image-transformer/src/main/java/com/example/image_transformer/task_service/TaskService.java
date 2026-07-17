package com.example.image_transformer.task_service;

import com.example.image_transformer.task.ImageTransformationTask;
import com.example.image_transformer.operations.ImageTransformationService;
import com.example.image_transformer.storage.FileStreamStorage;
import com.example.image_transformer.stream_processing.FileStreamProcessingManager;
import com.example.image_transformer.webhook.WebhookService;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class TaskService {
    private final WebhookService webhookService;
    private final ImageTransformationService transformationService;
    private final FileStreamProcessingManager fileStreamProcessingManager;
    private final FileStreamStorage storage;

    public void processTasks(@NotNull Collection<ImageTransformationTask> tasks) {
        // TODO: process in parallel
        tasks.forEach(this::processTask);
    }

    private void processTask(@NotNull ImageTransformationTask task){
        fileStreamProcessingManager.process(
                ()->storage.read(task.inputObject()),
                stream-> transformationService.transformFile(stream,task.operations()),
                stream-> storage.write(stream,task.outputObject())
        );
        webhookService.handleCallback(task);
    }
    // TODO unit test
}
