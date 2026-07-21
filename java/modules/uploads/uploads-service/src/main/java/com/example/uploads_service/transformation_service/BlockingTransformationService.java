package com.example.uploads_service.transformation_service;

import com.example.uploads_api.transformations.dto.ImageTransformationTaskGroupDTO;
import com.example.uploads_api.transformations.dto.VideoTransformationTaskDTO;
import com.example.uploads_api.transformations.dto.VideoTransformationTaskGroupDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;

import java.util.Collection;
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
    public void transformImages(ImageTransformationTaskGroupDTO transformations) {
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
    public void transformVideos(Collection<VideoTransformationTaskDTO> transformations) {
        videoApi.transformAll(new VideoTransformationTaskGroupDTO(transformations));
    }
}
