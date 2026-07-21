package com.example.uploads_api.transformations.mappers;

import com.example.uploads_api.transformations.dto.ImageTransformationTaskSpecDTO;
import com.example.uploads_api.transformations.path.TransformationPathManager;
import com.example.uploads_api.transformations.sources.ImageTransformationSource;
import com.example.uploads_api.uploads.Upload;
import org.jspecify.annotations.NonNull;

public final class ImageTransformationSourceMapper {
    private ImageTransformationSourceMapper() {
    }

    public static @NonNull ImageTransformationTaskSpecDTO createTaskDTO(
            @NonNull ImageTransformationSource source,
            @NonNull Upload original
    ) {
        return new ImageTransformationTaskSpecDTO(
                TransformationPathManager.getOutputObject(original, source),
                source.getName(),
                source.isLazy(),
                source.getOperations().getLimitWidth(),
                source.getOperations().getLimitHeight(),
                source.getOperations().getFormat(),
                source.getOperations().getQuality(),
                source.getOperations().getAspectRatio(),
                original.id()
        );
    }
}
