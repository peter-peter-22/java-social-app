package com.example.image_transformer.task;

import com.example.uploads_api.transformations.dto.ImageTransformationTaskGroupDTO;
import com.example.uploads_api.transformations.dto.ImageTransformationTaskSpecDTO;
import com.example.uploads_api.transformations.operations.ImageTransformationOperations;
import org.jspecify.annotations.NonNull;

public class ImageTransformationTaskMapper {
    public static @NonNull ImageTransformationTask createFromDTO(@NonNull ImageTransformationTaskSpecDTO dto) {
        return new ImageTransformationTask(
                ImageTransformationOperations.builder()
                        .format(dto.format())
                        .quality(dto.quality())
                        .aspectRatio(dto.aspectRatio())
                        .limitHeight(dto.limitHeight())
                        .limitWidth(dto.limitWidth())
                        .build(),
                dto.outputObject(),
                dto.name(),
                dto.lazy(),
                dto.uploadId()
        );
    }

    public static @NonNull ImageTransformationTaskGroup createFromGroupedDTO(@NonNull ImageTransformationTaskGroupDTO dto) {
        var tasks = dto.tasks().stream()
                .map(ImageTransformationTaskMapper::createFromDTO)
                .toList();
        return new ImageTransformationTaskGroup(dto.inputObject(), tasks);
    }
}
