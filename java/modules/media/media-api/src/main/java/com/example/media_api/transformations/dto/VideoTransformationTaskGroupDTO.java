package com.example.media_api.transformations.dto;

import java.util.Collection;

public record VideoTransformationTaskGroupDTO(
        Collection<VideoTransformationTaskDTO> tasks
) {
}
