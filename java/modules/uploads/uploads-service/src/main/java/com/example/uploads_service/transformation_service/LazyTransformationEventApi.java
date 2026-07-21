package com.example.uploads_service.transformation_service;

import com.example.uploads_api.transformations.dto.ImageTransformationTaskGroupDTO;
import com.example.uploads_api.transformations.dto.VideoTransformationTaskDTO;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

@Component
public class LazyTransformationEventApi {
    public void queueImage(@NonNull ImageTransformationTaskGroupDTO task) {
        System.out.println("Sending event: " + task);
    }

    public void queueVideo(@NonNull VideoTransformationTaskDTO task) {
        System.out.println("Sending event: " + task);
    }
}
