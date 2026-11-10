package com.example.uploads_service.processing;

import com.example.uploads_api.dto.ImageProcessingTask;
import org.jspecify.annotations.NullMarked;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
@EnableConfigurationProperties({UploadProcessingProperties.class})
@NullMarked
public class MediaWorkerService {
    private final RestClient imageRestClient;
    private final RestClient videoRestClient;

    @Autowired
    MediaWorkerService(UploadProcessingProperties uploadProcessingProperties) {
        this.imageRestClient = RestClient.create(uploadProcessingProperties.imageProcessingUrl());
        this.videoRestClient = RestClient.create(uploadProcessingProperties.videoProcessingUrl());
    }

    public void processImageBlocking(ImageProcessingTask task) {
        imageRestClient.post()
                .uri("/process")
                .body(task)
                .retrieve()
                .toBodilessEntity();
    }

    public void processVideoBlocking(ImageProcessingTask task) {
        videoRestClient.post()
                .uri("/process")
                .body(task)
                .retrieve()
                .toBodilessEntity();
    }

    public void processImageAsync(ImageProcessingTask task) {
// TODO implement
    }

    public void processVideoAsync(ImageProcessingTask task) {
// TODO implement
    }
}
