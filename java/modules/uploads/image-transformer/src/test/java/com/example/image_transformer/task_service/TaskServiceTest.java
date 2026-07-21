package com.example.image_transformer.task_service;

import com.example.image_transformer.operations.ImageTransformationService;
import com.example.image_transformer.storage.FileStreamStorage;
import com.example.image_transformer.stream_processing.FileStreamProcessingManager;
import com.example.image_transformer.task.ImageTransformationTask;
import com.example.image_transformer.task.ImageTransformationTaskGroup;
import com.example.image_transformer.webhook.WebhookService;
import com.example.uploads_api.transformations.operations.ImageTransformationOperations;
import com.example.uploads_api.uploads.ObjectLocation;
import com.example.uploads_api.uploads.UploadId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {
    private static final ObjectLocation INPUT = new ObjectLocation("original.jpg", "images");
    private static final byte[] SOURCE = {1, 2, 3};

    @Mock
    private WebhookService webhookService;
    @Mock
    private ImageTransformationService transformationService;
    @Mock
    private FileStreamStorage storage;

    @Test
    void readsSharedInputOnceAndReplaysItForEveryTask() throws Exception {
        var processedInputs = new java.util.ArrayList<byte[]>();
        when(storage.read(INPUT)).thenReturn(new ByteArrayInputStream(SOURCE));
        when(transformationService.transformFile(any(), any())).thenAnswer(invocation -> {
            InputStream input = invocation.getArgument(0);
            processedInputs.add(input.readAllBytes());
            return new ByteArrayInputStream(new byte[]{4});
        });

        var service = new TaskService(
                webhookService,
                transformationService,
                new FileStreamProcessingManager(),
                storage
        );
        var group = new ImageTransformationTaskGroup(INPUT, List.of(task("first"), task("second")));

        service.processTasks(group);

        verify(storage).read(INPUT);
        assertThat(processedInputs).containsExactly(SOURCE, SOURCE);
    }

    private static ImageTransformationTask task(String name) {
        return new ImageTransformationTask(
                ImageTransformationOperations.builder().build(),
                new ObjectLocation(name + ".jpg", "outputs"),
                name,
                false,
                new UploadId(UUID.randomUUID())
        );
    }
}
