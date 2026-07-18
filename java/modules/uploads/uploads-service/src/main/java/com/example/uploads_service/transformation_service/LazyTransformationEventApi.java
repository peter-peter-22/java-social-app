package com.example.uploads_service.transformation_service;

import com.example.uploads_api.transformations.dto.ImageTransformationTaskDTO;
import com.example.uploads_api.transformations.dto.VideoTransformationTaskDTO;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Component
public class LazyTransformationEventApi {
    public void queueImage(@NotNull ImageTransformationTaskDTO task){
        System.out.println("Sending event: " + task);
    }

    public void queueVideo(@NotNull VideoTransformationTaskDTO task){
        System.out.println("Sending event: " + task);
    }
}
