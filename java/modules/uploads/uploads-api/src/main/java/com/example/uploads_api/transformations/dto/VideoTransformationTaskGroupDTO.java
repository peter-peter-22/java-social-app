package com.example.uploads_api.transformations.dto;

import java.util.Collection;

public record VideoTransformationTaskGroupDTO(
        Collection<VideoTransformationTaskDTO> tasks
) {
}
