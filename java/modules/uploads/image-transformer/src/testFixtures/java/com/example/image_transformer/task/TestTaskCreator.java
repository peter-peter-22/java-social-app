package com.example.image_transformer.task;

import com.example.uploads_api.transformations.operations.ImageTransformationOperations;
import com.example.uploads_api.uploads.ObjectLocation;
import org.jspecify.annotations.Nullable;

import java.util.UUID;
import java.util.function.Consumer;

public class TestTaskCreator {
    public static ImageTransformationTask createTask(@Nullable Consumer<ImageTransformationTask.ImageTransformationTaskBuilder> customizer) {
        var builder = ImageTransformationTask.builder()
                .outputObject(new ObjectLocation(UUID.randomUUID().toString(), "output-bucket"))
                .operations(ImageTransformationOperations.builder().build())
                .name(UUID.randomUUID().toString())
                .uploadId(new com.example.uploads_api.uploads.UploadId(UUID.randomUUID()))
                .lazy(false);

        if (customizer != null)
            customizer.accept(builder);

        return builder.build();
    }
}
