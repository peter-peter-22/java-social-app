package com.example.media.uploads.transformations;

import com.example.media.api.transformations.api.MediaTransformerEndpoints;
import com.example.media.api.transformations.api.UploadTransformationDTO;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@EnableConfigurationProperties(TransformationProperties.class)
public class VideoTransformerRestApi extends ParralelTransformationApi {
    private final RestClient restClient;

    VideoTransformerRestApi(@NotNull @Autowired TransformationProperties properties) {
        this.restClient = RestClient.create(properties.videoTransformerUrl());
    }

    @Override
    public void call(@NonNull UploadTransformationDTO body) {
        restClient.post()
                .uri(MediaTransformerEndpoints.TRANSFORM)
                .body(body)
                .retrieve()
                .toBodilessEntity();
    }
}
