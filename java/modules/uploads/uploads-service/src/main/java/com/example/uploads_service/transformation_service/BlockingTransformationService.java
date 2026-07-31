package com.example.uploads_service.transformation_service;

import com.example.uploads_api.transformations.tasks.ImageTransformationTaskGroup;
import com.example.uploads_api.transformations.tasks.VideoTransformationTaskGroup;
import lombok.RequiredArgsConstructor;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
class BlockingTransformationService {
    private final BlockingImageTransformerRestApi imageApi;
    private final BlockingVideoTransformerRestApi videoApi;

    /**
     * Send all transformations to the image transformer, wait for the result.
     */
    @Retryable(
            value = HttpServerErrorException.InternalServerError.class,
            maxRetries = 2,
            delay = 1,
            maxDelay = 1,
            timeUnit = TimeUnit.SECONDS
    )
    public void transformImages(ImageTransformationTaskGroup transformations) {
        imageApi.transformAll(transformations);
    }

    /**
     * Send all transformations to the video transformer, wait for the result.
     */
    @Retryable(
            value = HttpServerErrorException.InternalServerError.class,
            maxRetries = 2,
            delay = 1,
            maxDelay = 1,
            timeUnit = TimeUnit.SECONDS
    )
    public void transformVideos(VideoTransformationTaskGroup transformations) {
        videoApi.transformAll(transformations);
    }
}
