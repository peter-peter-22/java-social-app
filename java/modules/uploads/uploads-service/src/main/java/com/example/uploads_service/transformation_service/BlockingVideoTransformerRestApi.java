package com.example.uploads_service.transformation_service;

import com.example.uploads_api.transformations.tasks.VideoTransformationTaskGroup;
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
    BlockingVideoTransformerRestApi(@NonNull TransformationProperties properties) {
        this.restClient = RestClient.create(properties.videoTransformerUrl());
    }

    /**
     * In practice, video transformations should not be blocking.
     */
    public void transformAll(@NonNull VideoTransformationTaskGroup body) {
        restClient.post()
                .uri("/transform")
                .body(body)
                .retrieve()
                .toBodilessEntity();
    }
}
