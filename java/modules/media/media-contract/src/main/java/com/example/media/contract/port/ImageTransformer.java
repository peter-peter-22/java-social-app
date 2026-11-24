package com.example.media.contract.port;

import com.example.media.contract.worker.ImageTransformCommand;
import com.example.media.contract.worker.ImageTransformResult;

/**
 * A media worker, from the API's point of view.
 *
 * <p>{@link #transform} is the blocking call. The worker allocates fresh result asset ids on every
 * execution, so a retry writes to new object keys and cannot collide with a partially completed
 * previous execution. The blocking adapter retries recoverable transport and worker failures up to
 * its configured limit; the caller marks the attempt failed when the call finally fails.</p>
 *
 * <p>{@link #transformAsync} is the queued call: it publishes the same command and returns
 * immediately. Every queue delivery is a new worker execution with fresh result asset ids, so a
 * redelivery is safe, and the completion the worker sends back is idempotent on the attempt id.</p>
 */
public interface ImageTransformer {

    /**
     * Downloads the source, transforms it, and uploads the results under newly allocated asset
     * prefixes. The worker does not register rows, change lifetimes, or finalize the attempt.
     */
    ImageTransformResult transform(ImageTransformCommand command);

    /**
     * Publishes the command to the queue and returns immediately. The worker runs the same
     * transformation as {@link #transform} and calls the API completion endpoint with the result.
     */
    void transformAsync(ImageTransformCommand command);
}