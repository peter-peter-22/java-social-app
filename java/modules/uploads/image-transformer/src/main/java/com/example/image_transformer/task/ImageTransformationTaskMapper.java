package com.example.image_transformer.task;

import com.example.uploads_api.transformations.dto.ImageTransformationTaskGroupDTO;
import com.example.uploads_api.transformations.operations.ImageTransformationOperations;
import com.example.uploads_api.uploads.UploadId;
import jakarta.validation.constraints.NotNull;
import org.jspecify.annotations.NonNull;

public class ImageTransformationTaskMapper {
    private static ImageTransformationTaskGroup.@NonNull Task createFromDTO(ImageTransformationTaskGroupDTO.@NonNull TransformationParameters dto, @NotNull UploadId uploadId) {
        return new ImageTransformationTaskGroup.Task(
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
                uploadId
        );
    }

    public static @NonNull ImageTransformationTaskGroup createFromGroupedDTO(@NonNull ImageTransformationTaskGroupDTO dto) {
        var tasks = dto.tasks().stream()
                .map(task -> createFromDTO(task, dto.uploadId()))
                .toList();
        return new ImageTransformationTaskGroup(dto.inputObject(), tasks);
    }
}
