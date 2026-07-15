package com.example.image_transformer.task;

import com.example.media_api.transformations.dto.ImageTransformationTaskDTO;
import com.example.media_api.transformations.operations.ImageTransformationOperations;
import org.jspecify.annotations.NonNull;

public class ImageTransformationTaskMapper {
    public static ImageTransformationTask createFromDTO(@NonNull ImageTransformationTaskDTO dto) {
        return new ImageTransformationTask(
                ImageTransformationOperations.builder()
                        .format(dto.format())
                        .quality(dto.quality())
                        .aspectRatio(dto.aspectRatio())
                        .limitHeight(dto.limitHeight())
                        .limitWidth(dto.limitWidth())
                        .build(),
                dto.inputObject(),
                dto.outputObject(),
                dto.name(),
                dto.lazy(),
                dto.uploadId()
        );
    }
}
