package com.example.media.contract.port;

/**
 * Decides whether a worker failure is transient enough to retry.
 *
 * <p>The blocking HTTP client and the async queue path must classify failures the same way, so the
 * retry decision has one policy shape. The concrete exception sets live in the deployables, where
 * the framework and storage types are known; the classification itself is not complete yet.</p>
 */
public interface FailureClassifier {

    /**
     * Whether the failure is recoverable (a transport or server error) rather than permanent.
     * Recoverable failures are retried up to the configured bound; an unrecoverable failure cancels
     * the retries of the job.
     */
    boolean isRecoverable(Throwable failure);
}
