package com.example.media.contract.upload;

/**
 * The state of an upload attempt.
 * The names are the values stored in the registry's status column; do not rename them.
 */
public enum UploadAttemptStatus {
    /** An attempt was created. It is being processed or will fail. Initial status. */
    PENDING,
    /**
     * The attempt is handed to a worker. Reserved for the async slice; nothing writes it yet.
     */
    PROCESSING,
    /** The attempt resulted in the creation of an upload. */
    SUCCESS,
    /**
     * The attempt failed: the worker reported an unrecoverable error, or the retries were
     * exhausted. Reserved for the failure-policy slice; nothing writes it yet.
     */
    FAILED
}