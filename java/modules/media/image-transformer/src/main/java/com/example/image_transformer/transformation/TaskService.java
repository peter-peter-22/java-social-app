package com.example.image_transformer.transformation;

import com.example.image_transformer.task.ImageTransformationTask;
import com.example.image_transformer.operations.ImageTransformationService;
import com.example.image_transformer.storage.FileStreamStorage;
import com.example.image_transformer.stream_processing.FileStreamProcessingManager;
import com.example.image_transformer.task.ImageTransformationTaskMapper;
import com.example.image_transformer.webhook.WebhookService;
import com.example.media_api.transformations.dto.ImageTransformationTaskGroupDTO;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskService {
    private final WebhookService webhookService;
    private final ImageTransformationService transformationService;
    private final FileStreamProcessingManager fileStreamProcessingManager;
    private final FileStreamStorage storage;

    public void processTasks(@NotNull ImageTransformationTaskGroupDTO payload) {
        var tasks = convert(payload);
        // TODO: process in parallel
        // TODO: reset failed transformations
        tasks.forEach(this::processTask);
    }

    private List<ImageTransformationTask> convert(@NotNull ImageTransformationTaskGroupDTO payload){
        return payload.tasks().stream()
                .map(ImageTransformationTaskMapper::createFromDTO)
                .toList();
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
