package com.example.media.imageworker.completion;

import com.example.media.contract.worker.ImageTransformResult;
import org.jspecify.annotations.NullMarked;
import org.springframework.core.retry.RetryException;
import org.springframework.core.retry.RetryTemplate;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

/**
 * The worker's completion call back to the media API. It posts the transform result in the
 * contract's JSON shape to the completion endpoint, which runs the same finalize use case as the
 * blocking path.
 *
 * <p>The call is retried like the API's blocking worker call: transport failures and server errors
 * are retried up to the configured limit, a 4xx answer (the attempt can never accept the result) is
 * not retried. The message is acked only after this call succeeds, so a redelivery re-runs the
 * whole transform with fresh result asset ids.</p>
 */
@NullMarked
public class ApiCompletionClient {

    /** The endpoint of {@code com.example.media.api.web.WorkerCompletionController}. */
    private static final String COMPLETION_IMAGE_PATH = "/api/completion/image";

    private final RestClient restClient;
    private final RetryTemplate retryTemplate;

    public ApiCompletionClient(RestClient restClient, RetryTemplate retryTemplate) {
        this.restClient = restClient;
        this.retryTemplate = retryTemplate;
    }

    /** Reports a successful transform. Throws when the API could not accept the result. */
    public void complete(ImageTransformResult result) {
        try {
            retryTemplate.execute(() -> post(result));
        } catch (RetryException e) {
            var cause = e.getCause();
            if (cause instanceof RuntimeException runtimeException)
                throw runtimeException;
            if (cause instanceof Error error)
                throw error;
            throw new IllegalStateException(
                    "the completion call failed for attempt " + result.attemptId().value(), cause);
        }
    }

    private ResponseEntity<Void> post(ImageTransformResult result) {
        return restClient.post()
                .uri(COMPLETION_IMAGE_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .body(result)
                .retrieve()
                .toBodilessEntity();
    }
}
