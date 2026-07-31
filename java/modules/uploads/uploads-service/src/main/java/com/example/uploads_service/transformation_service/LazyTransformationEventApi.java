package com.example.uploads_service.transformation_service;

import com.example.uploads_api.transformations.tasks.ImageTransformationTaskGroup;
import com.example.uploads_api.transformations.tasks.VideoTransformationTaskGroup;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

@Component
public class LazyTransformationEventApi {
    public void queueImage(@NonNull ImageTransformationTaskGroup task) {
        System.out.println("Sending event: " + task);
    }

    public void queueVideo(@NonNull VideoTransformationTaskGroup task) {
        System.out.println("Sending event: " + task);
    }
}
