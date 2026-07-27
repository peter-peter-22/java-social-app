package com.example.uploads_api.transformations.mappers;

import com.example.uploads_api.transformations.dto.ImageTransformationTaskGroupDTO;
import com.example.uploads_api.transformations.path.TransformationPathManager;
import com.example.uploads_api.transformations.sources.ImageTransformationSource;
import com.example.uploads_api.uploads.Upload;
import org.jspecify.annotations.NonNull;

import java.util.Collection;

public final class ImageTransformationMapper {
    private static ImageTransformationTaskGroupDTO.@NonNull TransformationParameters createTaskDTO(
            @NonNull ImageTransformationSource source,
            @NonNull Upload original
    ) {
        return new ImageTransformationTaskGroupDTO.TransformationParameters(
                TransformationPathManager.getOutputObject(original, source),
                source.getName(),
                source.isLazy(),
                source.getOperations().getLimitWidth(),
                source.getOperations().getLimitHeight(),
                source.getOperations().getFormat(),
                source.getOperations().getQuality(),
                source.getOperations().getAspectRatio()
        );
    }

    public static @NonNull ImageTransformationTaskGroupDTO createTaskGroupDTO(
            @NonNull Upload original,
            @NonNull Collection<ImageTransformationSource> transformations
    ) {
        return new ImageTransformationTaskGroupDTO(
                original.objectLocation(),
                transformations.stream().map(el -> createTaskDTO(el, original)).toList(),
                original.id()
        );
    }
}
