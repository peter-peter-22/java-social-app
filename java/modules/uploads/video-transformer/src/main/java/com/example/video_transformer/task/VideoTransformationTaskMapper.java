package com.example.video_transformer.task;

import com.example.uploads_api.transformations.dto.VideoTransformationTaskGroupDTO;
import com.example.uploads_api.transformations.operations.VideoTransformationOperations;
import com.example.uploads_api.uploads.UploadId;
import jakarta.validation.constraints.NotNull;
import org.jspecify.annotations.NonNull;

public class VideoTransformationTaskMapper {
    private static VideoTransformationTaskGroup.@NonNull Task createFromDTO(VideoTransformationTaskGroupDTO.@NonNull TransformationParameters dto, @NotNull UploadId uploadId) {
        return new VideoTransformationTaskGroup.Task(
                VideoTransformationOperations.builder()
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

    public static @NonNull VideoTransformationTaskGroup createFromGroupedDTO(@NonNull VideoTransformationTaskGroupDTO dto) {
        var tasks = dto.tasks().stream()
                .map(task -> createFromDTO(task, dto.uploadId()))
                .toList();
        return new VideoTransformationTaskGroup(dto.inputObject(), tasks);
    }
}
