package com.example.media.contract.upload;

/**
 * The state of an upload attempt.
 * The names are the values stored in the registry's status column; do not rename them.
 */
public enum UploadAttemptStatus {
    /**
     * An attempt was created and is being processed, or will fail. Initial status.
     * The async slice does not move the attempt to a processing state: cleanup runs on the
     * creation dates of the objects and assets, so the attempt stays {@code PENDING} until its
     * terminal {@code SUCCESS} or {@code FAILED}.
     */
    PENDING,
    /** The attempt resulted in the creation of an upload. */
    SUCCESS,
    /**
     * The attempt failed: the worker reported an unrecoverable error, or the retries were
     * exhausted.
     */
    FAILED
}