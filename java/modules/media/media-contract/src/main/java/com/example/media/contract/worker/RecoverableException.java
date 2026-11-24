package com.example.media.contract.worker;

/**
 * A recoverable failure of an image transformation: a transient transport, server, or storage
 * error that a retry can fix. Every adapter marks the transient failures of its own vocabulary
 * with it, so the recoverable and the unrecoverable errors stay one shared list for both the
 * blocking HTTP transformation and the async queued transformation.
 *
 * <p>{@link FailureClassifier} turns the mark into the retry decision. Anything that is not marked
 * is unrecoverable and fails the attempt.</p>
 */
public class RecoverableException extends RuntimeException {

    public RecoverableException(String message, Throwable cause) {
        super(message, cause);
    }
}