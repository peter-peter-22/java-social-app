package com.example.media.uploads.transformations;

import com.example.media.common.transformations.UploadTransformation;
import com.example.media.common.transformations.UploadTransformationDTO;
import com.example.media.common.uploads.UploadId;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestClient;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Repository
@EnableConfigurationProperties(BlockingTransformationProperties.class)
class BlockingTransformationRepository {
    private final RestClient restClient;

    public BlockingTransformationRepository(@NotNull @Autowired BlockingTransformationProperties properties) {
        this.restClient = RestClient.create("properties.endpoint");
    }

    // TODO: add retry mechanism
    private void applyTransformation(UploadTransformationDTO body) {
        restClient.post()
                .uri("/transform")
                .body(body)
                .retrieve()
                .toBodilessEntity();
    }

    /**
     * Apply the selected transformations, wait until all are completed.
     */
    public void applyTransformations(UploadId uploadId, Collection<UploadTransformation> transformations) {
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
        try (executor) {
            List<CompletableFuture<Void>> futures = transformations.stream()
                    .map(transformation -> CompletableFuture.runAsync(() -> {
                        applyTransformation(UploadTransformationDTO.toDTO(transformation, uploadId));
                    }, executor))
                    .toList();

            CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).join();
        }
    }
}
