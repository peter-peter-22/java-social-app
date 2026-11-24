package com.example.media.contract.port;

import com.example.media.contract.worker.ImageTransformCommand;
import com.example.media.contract.worker.ImageTransformResult;

/**
 * A media worker's blocking call, from the API's point of view.
 *
 * <p>The worker allocates fresh result asset ids on every execution, so a retry writes to new object
 * keys and cannot collide with a partially completed previous execution. The blocking adapter
 * retries recoverable transport and worker failures up to its configured limit; the caller marks the
 * attempt failed when the call finally fails.</p>
 *
 * <p>The queued call is a separate, narrower port: {@link AsyncImageTransformer}.</p>
 */
public interface ImageTransformer {

    /**
     * Downloads the source, transforms it, and uploads the results under newly allocated asset
     * prefixes. The worker does not register rows, change lifetimes, or finalize the attempt.
     */
    ImageTransformResult transform(ImageTransformCommand command);
}