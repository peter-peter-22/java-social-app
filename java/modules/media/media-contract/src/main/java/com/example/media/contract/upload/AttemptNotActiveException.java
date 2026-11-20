package com.example.media.contract.upload;

/**
 * The attempt is not the active attempt of its process, or the process already has its upload.
 * A stale attempt must not finalize; see the finalize rules in {@code planning/uploads/upload-lifecycle.md}.
 */
public class AttemptNotActiveException extends RuntimeException {
    public AttemptNotActiveException(String message) {
        super(message);
    }
}