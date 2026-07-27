package com.example.uploads_service.transformation_service;

import com.example.uploads_api.transformations.dto.ImageTransformationTaskGroupDTO;
import com.example.uploads_api.transformations.dto.VideoTransformationTaskGroupDTO;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class LazyTransformationService {
    private final LazyTransformationEventApi api;

    /**
     * Send all transformations to the event queue.
     */
    @Retryable(
            value = HttpServerErrorException.InternalServerError.class,
            maxRetries = 2,
            delay = 1,
            maxDelay = 1,
            timeUnit = TimeUnit.SECONDS
    )
    public void queueImageTransformations(@NonNull ImageTransformationTaskGroupDTO tasks) {
        api.queueImage(tasks);
    }

    /**
     * Send all transformations to the event queue.
     */
    @Retryable(
            value = HttpServerErrorException.InternalServerError.class,
            maxRetries = 2,
            delay = 1,
            maxDelay = 1,
            timeUnit = TimeUnit.SECONDS
    )
    public void queueVideoTransformations(@NonNull VideoTransformationTaskGroupDTO tasks) {
        api.queueVideo(tasks);
    }
}
