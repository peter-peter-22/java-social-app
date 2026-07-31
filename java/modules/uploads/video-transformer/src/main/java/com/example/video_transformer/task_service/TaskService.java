package com.example.video_transformer.task_service;

import com.example.transformer_contracts.storage.FileStreamStorage;
import com.example.transformer_contracts.stream_processing.FileStreamProcessingManager;
import com.example.transformer_contracts.webhook.WebhookService;
import com.example.uploads_api.transformations.tasks.VideoTransformationTaskGroup;
import com.example.video_transformer.operations.VideoTransformationService;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TaskService {
    private final WebhookService webhookService;
    private final VideoTransformationService transformationService;
    private final FileStreamStorage storage;

    public void processTasks(@NonNull VideoTransformationTaskGroup group) {
        var source = FileStreamProcessingManager.readAllBytes(() -> storage.read(group.inputObject()));
        group.tasks().forEach(task -> processTask(task, source));
        webhookService.handleWebhookCalls(group);
    }

    // OPTIMIZE: should this be parallel?
    // TODO: CLEAN: this could be extracted to the contracts module
    private void processTask(VideoTransformationTaskGroup.@NonNull VideoTask task, byte[] source) {
        FileStreamProcessingManager.process(
                source,
                stream -> transformationService.transformFile(stream, task.operations()),
                stream -> storage.write(stream, task.outputObject())
        );
    }
}
