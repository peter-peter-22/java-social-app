package com.example.media.contract.port;

import com.example.media.contract.worker.ImageTransformCommand;
import com.example.media.contract.worker.ImageTransformResult;

/**
 * A media worker, from the API's point of view.
 *
 * <p>{@link #transform} is the blocking call. Failures are classified recoverable and
 * unrecoverable per the failure policy in {@code planning/uploads/upload-lifecycle.md}: the caller
 * retries recoverable failures up to a limit and marks the attempt failed when the retries are
 * exhausted or the error is unrecoverable. The async {@code enqueue} variant is added with the
 * async slice; do not build queue-shaped abstractions for it early.</p>
 */
public interface ImageTransformer {

    /**
     * Downloads the source, transforms it, and uploads the results under the reserved prefixes.
     * The worker does not register rows, change lifetimes, or finalize the attempt.
     */
    ImageTransformResult transform(ImageTransformCommand command);
}