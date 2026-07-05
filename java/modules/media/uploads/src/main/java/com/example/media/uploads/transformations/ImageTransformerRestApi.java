package com.example.media.uploads.transformations;

import com.example.media.common.transformations.api.MediaTransformerEndpoints;
import com.example.media.common.transformations.api.UploadTransformationDTO;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@EnableConfigurationProperties(TransformationProperties.class)
public class ImageTransformerRestApi extends ParralelTransformationApi {
    private final RestClient restClient;

    ImageTransformerRestApi(@NotNull @Autowired TransformationProperties properties) {
        this.restClient = RestClient.create(properties.imageTransformerUrl());
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
