package com.example.media.contract.port;

import com.example.media.contract.worker.ImageTransformCommand;

/**
 * A media worker's queued call, from the API's point of view: the fire-and-forget counterpart of the
 * blocking {@link ImageTransformer}.
 *
 * <p>It publishes the command and returns immediately. The worker runs the same transformation as
 * the blocking call and later calls the API completion endpoint with the result. Every queue
 * delivery is a new worker execution with fresh result asset ids, so a redelivery is safe, and the
 * completion is idempotent on the attempt id.</p>
 */
public interface AsyncImageTransformer {

    /**
     * Publishes the command to a worker queue and returns immediately. The worker registers nothing;
     * it reports its result to the API completion endpoint, which finalizes the attempt.
     */
    void transformAsync(ImageTransformCommand command);
}
