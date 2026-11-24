package com.example.media.api.worker;

import com.example.media.contract.port.ImageTransformer;
import com.example.media.contract.worker.ImageTransformCommand;
import com.example.media.contract.worker.ImageTransformResult;
import org.jspecify.annotations.NullMarked;
import org.springframework.core.retry.RetryException;
import org.springframework.core.retry.RetryTemplate;
import org.springframework.http.MediaType;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

/**
 * The blocking {@link ImageTransformer} port over HTTP: it posts the command to the image worker's
 * transform endpoint and reads back the result the port promises. The messages travel in the JSON
 * shape the contract defines.
 *
 * <p>The worker only downloads, transforms, and uploads, so the lifecycle stays here: the complete
 * use case turns the result into the finalize. The worker allocates fresh result asset ids on every
 * execution, so retrying the same command writes to new object keys and cannot collide with a
 * partially completed earlier attempt. The adapter retries recoverable transport failures
 * ({@link ResourceAccessException}) and worker server errors ({@link HttpServerErrorException}) up
 * to the configured limit. Client errors are not retried.</p>
 */
@NullMarked
public class HttpImageTransformer implements ImageTransformer {

    /** The endpoint of {@code com.example.media.imageworker.web.ImageTransformController}. */
    private static final String TRANSFORM_IMAGE_PATH = "/api/transform/image";

    private final RestClient restClient;
    private final RetryTemplate retryTemplate;

    /** The client must target one image worker's base URL. */
    public HttpImageTransformer(RestClient restClient, RetryTemplate retryTemplate) {
        this.restClient = restClient;
        this.retryTemplate = retryTemplate;
    }

    @Override
    public ImageTransformResult transform(ImageTransformCommand command) {
        try {
            return retryTemplate.execute(() -> post(command));
        } catch (RetryException e) {
            var cause = e.getCause();
            if (cause instanceof RuntimeException runtimeException)
                throw runtimeException;
            if (cause instanceof Error error)
                throw error;
            throw new IllegalStateException(
                    "the image worker call failed after retries for attempt " + command.attemptId().value(),
                    cause);
        }
    }

    @Override
    public void transformAsync(ImageTransformCommand command) {
        throw new UnsupportedOperationException("the blocking adapter does not enqueue commands");
    }

    private ImageTransformResult post(ImageTransformCommand command) {
        var result = restClient.post()
                .uri(TRANSFORM_IMAGE_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .body(command)
                .retrieve()
                .body(ImageTransformResult.class);
        if (result == null)
            throw new IllegalStateException(
                    "the image worker returned no result for attempt " + command.attemptId().value());
        return result;
    }
}