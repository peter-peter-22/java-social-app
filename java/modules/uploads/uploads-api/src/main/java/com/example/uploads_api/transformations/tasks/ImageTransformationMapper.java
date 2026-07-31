package com.example.uploads_api.transformations.tasks;

import com.example.uploads_api.transformations.object_keys.TransformationKeyManager;
import com.example.uploads_api.transformations.sources.ImageTransformationSource;
import com.example.uploads_api.uploads.Upload;
import org.jspecify.annotations.NonNull;

import java.util.Collection;

public final class ImageTransformationMapper {
    private static ImageTransformationTaskGroup.@NonNull ImageTask createTaskDTO(
            @NonNull ImageTransformationSource source,
            @NonNull Upload original
    ) {
        return new ImageTransformationTaskGroup.ImageTask(
                TransformationKeyManager.getOutputObject(original, source),
                source.getName(),
                source.isLazy(),
                source.getOperations()
        );
    }

    public static @NonNull ImageTransformationTaskGroup createTaskGroupDTO(
            @NonNull Upload original,
            @NonNull Collection<ImageTransformationSource> transformations
    ) {
        return new ImageTransformationTaskGroup(
                original.objectLocation(),
                transformations.stream().map(el -> createTaskDTO(el, original)).toList(),
                original.id()
        );
    }
}
