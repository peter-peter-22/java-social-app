package com.example.uploads_api.transformations.tasks;

import com.example.uploads_api.transformations.object_keys.TransformationKeyManager;
import com.example.uploads_api.transformations.sources.VideoTransformationSource;
import com.example.uploads_api.uploads.Upload;
import org.jspecify.annotations.NonNull;

import java.util.Collection;

public final class VideoTransformationMapper {
    private static VideoTransformationTaskGroup.@NonNull VideoTask createTaskDTO(
            @NonNull VideoTransformationSource source,
            @NonNull Upload original
    ) {
        return new VideoTransformationTaskGroup.VideoTask(
                TransformationKeyManager.getOutputObject(original, source),
                source.getName(),
                source.isLazy(),
                source.getOperations()
        );
    }

    public static @NonNull VideoTransformationTaskGroup createTaskGroupDTO(
            @NonNull Upload original,
            @NonNull Collection<VideoTransformationSource> transformations
    ) {
        return new VideoTransformationTaskGroup(
                original.objectLocation(),
                transformations.stream().map(el -> createTaskDTO(el, original)).toList(),
                original.id()
        );
    }
}
