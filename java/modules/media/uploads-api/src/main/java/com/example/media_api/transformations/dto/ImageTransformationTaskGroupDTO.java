package com.example.media_api.transformations.dto;

import java.util.Collection;

public record ImageTransformationTaskGroupDTO(
        Collection<ImageTransformationTaskDTO> tasks
) {
}
