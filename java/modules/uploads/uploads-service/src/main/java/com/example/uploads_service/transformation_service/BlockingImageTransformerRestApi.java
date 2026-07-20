package com.example.uploads_service.transformation_service;

import com.example.uploads_api.transformations.dto.ImageTransformationTaskGroupDTO;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@EnableConfigurationProperties(TransformationProperties.class)
public class BlockingImageTransformerRestApi {
    private final RestClient restClient;

    @Autowired
    BlockingImageTransformerRestApi(@NonNull TransformationProperties properties) {
        this.restClient = RestClient.create(properties.imageTransformerUrl());
    }

    public void transformAll(@NonNull ImageTransformationTaskGroupDTO body) {
        restClient.post()
                .uri("/transform")
                .body(body)
                .retrieve()
                .toBodilessEntity();
    }
}
