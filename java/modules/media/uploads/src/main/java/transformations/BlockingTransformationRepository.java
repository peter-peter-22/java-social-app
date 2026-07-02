package transformations;

import com.example.transformations.UploadTransformation;
import com.example.transformations.UploadTransformationDTO;
import com.example.uploads.UploadId;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class BlockingTransformationRepository {
    private final BlockingTransformationProperties properties;
    private final RestClient restClient = RestClient.create(properties.endpoint);

    // TODO: add retry mechanism
    private void applyTransformation(UploadTransformationDTO body) {
        restClient.post()
                .uri( "/transform")
                .body(body)
                .retrieve()
                .toBodilessEntity();
    }

    /** Apply the selected transformations, wait until all are completed. */
    public void applyTransformations(UploadId uploadId, Collection<UploadTransformation> transformations) {
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
        try (executor) {
            List<CompletableFuture<Void>> futures = transformations.stream()
                    .map(transformation -> CompletableFuture.runAsync(() -> {
                        applyTransformation( UploadTransformationDTO.toDTO(transformation, uploadId));
                    }, executor))
                    .toList();

            CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).join();
        }
    }
}
