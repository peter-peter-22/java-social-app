package com.example.media.imageworker.completion;

import com.example.media.contract.worker.FailureClassifier;
import com.example.media.contract.worker.ImageTransformResult;
import com.example.media.contract.worker.RecoverableException;
import org.jspecify.annotations.NullMarked;
import org.springframework.core.retry.RetryTemplate;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

/**
 * The worker's completion call back to the media API. It posts the transform result in the
 * contract's JSON shape to the completion endpoint, which runs the same finalize use case as the
 * blocking path.
 *
 * <p>The call is retried with the shared {@link FailureClassifier} conditions: the adapter marks
 * its own transient vocabulary (transport failures and server errors) as
 * {@link RecoverableException} and retries it up to the configured limit, a 4xx answer (the attempt
 * can never accept the result) is not marked and is not retried. The message is acked only after
 * this call succeeds, so a redelivery re-runs the whole transform with fresh result asset ids.</p>
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
        // invoke() rethrows the last failure when the retry policy is exhausted, so the caller
        // sees the marked exception itself, not a retry wrapper.
        retryTemplate.invoke(() -> post(result));
    }

    private ResponseEntity<Void> post(ImageTransformResult result) {
        try {
            return restClient.post()
                    .uri(COMPLETION_IMAGE_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(result)
                    .retrieve()
                    .toBodilessEntity();
        } catch (ResourceAccessException | HttpServerErrorException e) {
            throw new RecoverableException(
                    "the completion call failed transiently for attempt " + result.attemptId().value(), e);
        }
    }
}
