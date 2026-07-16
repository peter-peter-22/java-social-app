package com.example.image_transformer.task;

import com.example.media_api.transformations.operations.ImageTransformationOperations;
import com.example.media_api.uploads.ObjectLocation;
import org.jspecify.annotations.Nullable;

import java.util.UUID;
import java.util.function.Consumer;

public class TestTaskCreator {
    public static ImageTransformationTask createTask(@Nullable Consumer<ImageTransformationTask.ImageTransformationTaskBuilder> customizer) {
        var builder = ImageTransformationTask.builder()
                .inputObject(new ObjectLocation(UUID.randomUUID().toString(), "input-bucket"))
                .outputObject(new ObjectLocation(UUID.randomUUID().toString(), "output-bucket"))
                .operations(ImageTransformationOperations.builder().build())
                .name(UUID.randomUUID().toString())
                .lazy(false);

        if (customizer != null)
            customizer.accept(builder);

        return builder.build();
    }

    public static ImageTransformationTask createTask() {
        return createTask(null);
    }
}
