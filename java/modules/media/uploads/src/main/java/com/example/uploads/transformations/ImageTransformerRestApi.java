package com.example.uploads.transformations;

import com.example.media_api.transformations.task.UploadTransformationTask;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.HttpServerErrorException;

import java.util.concurrent.TimeUnit;

// OPTIMIZATION: should this be parallel on the worker server instead?
@Component
@EnableConfigurationProperties(TransformationProperties.class)
public class ImageTransformerRestApi extends ParallelTransformationApi {
    private final RestClient restClient;

    @Autowired
    ImageTransformerRestApi(@NotNull TransformationProperties properties) {
        this.restClient = RestClient.create(properties.imageTransformerUrl());
    }

    @Override
    @Retryable(
            value = HttpServerErrorException.InternalServerError.class,
            maxRetries = 2,
            delay = 1,
            maxDelay = 1,
            timeUnit = TimeUnit.SECONDS
    )
    public void call(@NonNull UploadTransformationTask body) {
        restClient.post()
                .uri("/transform")
                .body(body)
                .retrieve()
                .toBodilessEntity();
    }
}
