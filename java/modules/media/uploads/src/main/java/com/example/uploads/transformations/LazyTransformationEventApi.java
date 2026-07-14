package com.example.uploads.transformations;

import com.example.media_api.transformations.dto.ImageTransformationTaskDTO;
import com.example.media_api.transformations.dto.VideoTransformationTaskDTO;
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
