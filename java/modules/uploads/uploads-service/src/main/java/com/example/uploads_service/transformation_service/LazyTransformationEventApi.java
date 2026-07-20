package com.example.uploads_service.transformation_service;

import com.example.uploads_api.transformations.dto.ImageTransformationTaskDTO;
import com.example.uploads_api.transformations.dto.VideoTransformationTaskDTO;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

@Component
public class LazyTransformationEventApi {
    public void queueImage(@NonNull ImageTransformationTaskDTO task) {
        System.out.println("Sending event: " + task);
    }

    public void queueVideo(@NonNull VideoTransformationTaskDTO task) {
        System.out.println("Sending event: " + task);
    }
}
