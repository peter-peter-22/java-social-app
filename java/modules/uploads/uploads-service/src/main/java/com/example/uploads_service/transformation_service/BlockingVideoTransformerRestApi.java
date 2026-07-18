package com.example.uploads_service.transformation_service;

import com.example.uploads_api.transformations.dto.VideoTransformationTaskGroupDTO;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@EnableConfigurationProperties(TransformationProperties.class)
public class BlockingVideoTransformerRestApi {
    private final RestClient restClient;

    @Autowired
    BlockingVideoTransformerRestApi(@NotNull TransformationProperties properties) {
        this.restClient = RestClient.create(properties.videoTransformerUrl());
    }

    public void transformAll(@NonNull VideoTransformationTaskGroupDTO body) {
        restClient.post()
                .uri("/transform")
                .body(body)
                .retrieve()
                .toBodilessEntity();
    }
}
