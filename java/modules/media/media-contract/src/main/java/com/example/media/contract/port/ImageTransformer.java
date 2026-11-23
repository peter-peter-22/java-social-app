package com.example.media.contract.port;

import com.example.media.contract.worker.ImageTransformCommand;
import com.example.media.contract.worker.ImageTransformResult;

/**
 * A media worker, from the API's point of view.
 *
 * <p>{@link #transform} is the blocking call. The worker allocates fresh result asset ids on every
 * execution, so a retry writes to new object keys and cannot collide with a partially completed
 * previous execution. The blocking adapter retries recoverable transport and worker failures up to
 * its configured limit; the caller marks the attempt failed when the call finally fails. The async
 * {@code enqueue} variant is added with the async slice; do not build queue-shaped abstractions for
 * it early.</p>
 */
public interface ImageTransformer {

    /**
     * Downloads the source, transforms it, and uploads the results under newly allocated asset
     * prefixes. The worker does not register rows, change lifetimes, or finalize the attempt.
     */
    ImageTransformResult transform(ImageTransformCommand command);
}