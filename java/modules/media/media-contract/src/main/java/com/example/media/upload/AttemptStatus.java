package com.example.media.upload;

/**
 * Lifecycle of one attempt. {@link #PROCESSING} is set before a command is queued.
 * {@link #FAILED} is set when retries are exhausted or the error is unrecoverable.
 */
public enum AttemptStatus {
    PENDING,
    PROCESSING,
    SUCCESS,
    FAILED
}
