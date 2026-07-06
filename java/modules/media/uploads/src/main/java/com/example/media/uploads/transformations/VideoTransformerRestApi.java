package com.example.media.uploads.transformations;

import com.example.media_api.transformations.api.MediaTransformerEndpoints;
import com.example.media_api.transformations.api.UploadTransformationDTO;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;

import java.util.concurrent.TimeUnit;

@Component
@EnableConfigurationProperties(TransformationProperties.class)
public class VideoTransformerRestApi extends ParallelTransformationApi {
    private final RestClient restClient;

    VideoTransformerRestApi(@NotNull @Autowired TransformationProperties properties) {
        this.restClient = RestClient.create(properties.videoTransformerUrl());
    }

    @Override
    @Retryable(value = HttpServerErrorException.InternalServerError.class, maxRetries = 3, delay = 1, timeUnit = TimeUnit.SECONDS)
    public void call(@NonNull UploadTransformationDTO body) {
        restClient.post()
                .uri(MediaTransformerEndpoints.TRANSFORM)
                .body(body)
                .retrieve()
                .toBodilessEntity();
    }
}
