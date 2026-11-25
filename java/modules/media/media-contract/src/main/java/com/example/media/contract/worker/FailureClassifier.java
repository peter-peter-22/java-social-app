package com.example.media.contract.worker;

/**
 * The shared retry conditions of an image transformation.
 *
 * <p>The blocking HTTP call, the async queue delivery, and the worker's completion call run the
 * same task, so they retry the same failures: one policy here, not one per adapter. A failure is
 * recoverable when its cause chain carries a {@link RecoverableException}, which the adapters throw
 * for the transient failures of their own vocabulary (transport errors, server errors, transient
 * storage errors). Everything else is unrecoverable and cancels the retries of the attempt.</p>
 */
public final class FailureClassifier {

    private FailureClassifier() {
    }

    /**
     * Whether the failure is recoverable (a transient transport, server, or storage error) rather
     * than permanent. Recoverable failures are retried up to the configured bound; an unrecoverable
     * failure cancels the retries of the attempt.
     */
    public static boolean isRecoverable(Throwable failure) {
        for (var cause = failure; cause != null; cause = cause.getCause())
            if (cause instanceof RecoverableException)
                return true;
        return false;
    }
}