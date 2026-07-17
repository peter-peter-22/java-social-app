package com.example.uploads_persistence.transformations;

import com.example.media_api.transformations.dto.ImageTransformationTaskDTO;
import com.example.media_api.transformations.dto.VideoTransformationTaskDTO;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
public class LazyTransformationService {
    private final LazyTransformationEventApi api;

    private <T> void sendInParallel(@NotNull Collection<T> messages, @NotNull Consumer<T> consumer) {
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
        try (executor) {
            var futures = messages.stream()
                    .map(message -> CompletableFuture.runAsync(() -> consumer.accept(message), executor))
                    .toArray(CompletableFuture[]::new);

            CompletableFuture.allOf(futures).join();
        }
    }

    /**
     * Send all transformations to the event queue.
     */
    @Retryable(
            value = HttpServerErrorException.InternalServerError.class,
            maxRetries = 2,
            delay = 1,
            maxDelay = 1,
            timeUnit = TimeUnit.SECONDS
    )
    public void queueImageTransformations(@NotNull Collection<ImageTransformationTaskDTO> tasks) {
        sendInParallel(tasks, api::queueImage);
    }

    /**
     * Send all transformations to the event queue.
     */
    @Retryable(
            value = HttpServerErrorException.InternalServerError.class,
            maxRetries = 2,
            delay = 1,
            maxDelay = 1,
            timeUnit = TimeUnit.SECONDS
    )
    public void queueVideoTransformations(@NotNull Collection<VideoTransformationTaskDTO> tasks) {
        sendInParallel(tasks, api::queueVideo);
    }
}
