package com.example.media.contract.upload;

/** How an attempt ends. The terminal states of {@link UploadAttemptStatus}. */
public enum AttemptOutcome {
    /** The attempt produced the final upload. */
    SUCCESS,
    /**
     * The attempt gave up: the failure was unrecoverable, or the retries were exhausted.
     * See the failure policy in {@code planning/uploads/upload-lifecycle.md}.
     */
    FAILED
}