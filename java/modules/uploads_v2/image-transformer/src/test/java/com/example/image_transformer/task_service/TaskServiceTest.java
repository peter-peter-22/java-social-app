package com.example.image_transformer.task_service;

import com.example.image_transformer.operations.ImageTransformationService;
import com.example.transformer_contracts.storage.FileStreamStorage;
import com.example.transformer_contracts.webhook.WebhookService;
import com.example.uploads_api.transformations.tasks.ImageTransformationTaskGroup;
import com.example.uploads_api.uploads.ObjectLocation;
import com.example.uploads_api.utils.TestTransformationTaskGroupCreator;
import com.example.uploads_api.v2.transformations.operations.ImageTransformationOperations;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// CLEAN: should this be simplified by checking only the call count?
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

    /**
     * The ImageTransformationService should call the storage read function only once per group
     * and the write function for each task.
     */
    @Test
    @SuppressWarnings("resource")
    // The file stream is not used, no try block is necessary
    void readsSharedInputOnceAndReplaysItForEveryTask() {
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
                storage
        );
        var group = TestTransformationTaskGroupCreator.createImageTransformationTaskGroup(
                c -> c.inputObject(INPUT)
                        .tasks(List.of(task("a"), task("b")))
        );

        service.processTask(group);

        verify(storage, times(1)).read(any());
        verify(storage, times(2)).write(any(), any());

        verify(storage).read(INPUT);
        assertThat(processedInputs).containsExactly(SOURCE, SOURCE);
    }

    private static ImageTransformationTaskGroup.@NonNull ImageTask task(@NonNull String name) {
        return ImageTransformationTaskGroup.ImageTask.builder()
                .name(name)
                .outputObject(new ObjectLocation(name + ".jpg", "outputs"))
                .operations(ImageTransformationOperations.builderWithDefaults().build())
                .build();
    }
}
