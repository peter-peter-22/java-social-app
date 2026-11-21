package com.example.media.api.worker;

import com.example.media.contract.port.ImageTransformer;
import com.example.media.contract.worker.ImageTransformCommand;
import com.example.media.contract.worker.ImageTransformResult;
import org.jspecify.annotations.NullMarked;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

/**
 * The blocking {@link ImageTransformer} port over HTTP: it posts the command to the image worker's
 * transform endpoint and reads back the result the port promises. The messages travel in the JSON
 * shape the contract defines.
 *
 * <p>The worker only downloads, transforms, and uploads, so the lifecycle stays here: the complete
 * use case turns the result into the finalize. The failures of the call propagate to the caller;
 * the recoverable and unrecoverable classification and the retry limits of the failure policy land
 * with the retrying caller. The Spring wiring of this adapter lands with the adapters step.</p>
 */
@NullMarked
public class HttpImageTransformer implements ImageTransformer {

    /** The endpoint of {@code com.example.media.imageworker.web.ImageTransformController}. */
    private static final String TRANSFORM_IMAGE_PATH = "/api/transform/image";

    private final RestClient restClient;

    /** The client must target one image worker's base URL. */
    public HttpImageTransformer(RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public ImageTransformResult transform(ImageTransformCommand command) {
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