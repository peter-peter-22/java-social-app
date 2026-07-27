package com.example.uploads_api.transformations.mappers;

import com.example.uploads_api.transformations.dto.VideoTransformationTaskGroupDTO;
import com.example.uploads_api.transformations.path.TransformationPathManager;
import com.example.uploads_api.transformations.sources.VideoTransformationSource;
import com.example.uploads_api.uploads.Upload;
import org.jspecify.annotations.NonNull;

import java.util.Collection;

public final class VideoTransformationMapper {
    private static VideoTransformationTaskGroupDTO.@NonNull TransformationParameters createTaskDTO(
            @NonNull VideoTransformationSource source,
            @NonNull Upload original
    ) {
        return new VideoTransformationTaskGroupDTO.TransformationParameters(
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

    public static @NonNull VideoTransformationTaskGroupDTO createTaskGroupDTO(
            @NonNull Upload original,
            @NonNull Collection<VideoTransformationSource> transformations
    ) {
        return new VideoTransformationTaskGroupDTO(
                original.objectLocation(),
                transformations.stream().map(el -> createTaskDTO(el, original)).toList(),
                original.id()
        );
    }
}
