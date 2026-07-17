package com.example.image_transformer.task;

import com.example.uploads_api.transformations.dto.ImageTransformationTaskDTO;
import com.example.uploads_api.transformations.dto.ImageTransformationTaskGroupDTO;
import com.example.uploads_api.transformations.operations.ImageTransformationOperations;
import org.jspecify.annotations.NonNull;

import java.util.List;

public class ImageTransformationTaskMapper {
    public static @NonNull ImageTransformationTask createFromDTO(@NonNull ImageTransformationTaskDTO dto) {
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

    public static @NonNull List<ImageTransformationTask> createFromGroupedDTO(@NonNull ImageTransformationTaskGroupDTO dto) {
        return dto.tasks().stream()
                .map(ImageTransformationTaskMapper::createFromDTO)
                .toList();
    }
}
