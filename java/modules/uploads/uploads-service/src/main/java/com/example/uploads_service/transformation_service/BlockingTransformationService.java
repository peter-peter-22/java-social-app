package com.example.uploads_service.transformation_service;

import com.example.uploads_api.transformations.dto.*;
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
        var inputObjects = transformations.stream()
                .map(ImageTransformationTaskDTO::inputObject)
                .distinct()
                .toList();
        if (inputObjects.size() != 1)
            throw new IllegalArgumentException("Image transformations must share one input object");

        var inputObject = inputObjects.getFirst();
        var specs = transformations.stream()
                .map(this::toSpec)
                .toList();
        imageApi.transformAll(new ImageTransformationTaskGroupDTO(inputObject, specs));
    }

    private ImageTransformationTaskSpecDTO toSpec(ImageTransformationTaskDTO task) {
        return new ImageTransformationTaskSpecDTO(
                task.outputObject(), task.name(), task.lazy(), task.limitWidth(), task.limitHeight(),
                task.format(), task.quality(), task.aspectRatio(), task.uploadId()
        );
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
