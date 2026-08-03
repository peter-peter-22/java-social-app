package com.example.video_transformer.task_service;

import com.example.transformer_contracts.storage.FileStreamStorage;
import com.example.transformer_contracts.stream_processing.FileStreamProcessingManager;
import com.example.transformer_contracts.webhook.WebhookService;
import com.example.uploads_api.transformations.tasks.VideoTransformationTaskGroup;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TaskService {
    private final WebhookService webhookService;
    private final FileStreamStorage storage;

    public void processTasks(@NonNull VideoTransformationTaskGroup group) {
        var source = FileStreamProcessingManager.readAllBytes(() -> storage.read(group.inputObject()));
        // TODO should be parallel?
        // TODO: CLEAN: refractor
        group.tasks().forEach(task -> {
            FileStreamProcessingManager.process(
                    source,
                    stream -> transformationService.transformFile(stream, task.operations()),
                    stream -> storage.write(stream, task.outputObject())
            );
            webhookService.handleWebhookCall(task, group);
        });
    }
}
