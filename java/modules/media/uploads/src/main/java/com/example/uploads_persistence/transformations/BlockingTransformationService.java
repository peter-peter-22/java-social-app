package com.example.uploads_persistence.transformations;

import com.example.media_api.transformations.dto.ImageTransformationTaskDTO;
import com.example.media_api.transformations.dto.ImageTransformationTaskGroupDTO;
import com.example.media_api.transformations.dto.VideoTransformationTaskDTO;
import com.example.media_api.transformations.dto.VideoTransformationTaskGroupDTO;
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
    public void transformImages(Collection<ImageTransformationTaskDTO> transformations) {
        imageApi.transformAll(new ImageTransformationTaskGroupDTO(transformations));
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
