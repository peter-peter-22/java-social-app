package com.example.uploads_service.transformation_service;

import com.example.uploads_api.transformations.tasks.ImageTransformationTaskGroup;
import com.example.uploads_api.transformations.tasks.VideoTransformationTaskGroup;
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
    public void queueImageTransformations(@NonNull ImageTransformationTaskGroup tasks) {
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
    public void queueVideoTransformations(@NonNull VideoTransformationTaskGroup tasks) {
        api.queueVideo(tasks);
    }
}
