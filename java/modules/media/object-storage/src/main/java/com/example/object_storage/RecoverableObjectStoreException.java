package com.example.object_storage;

/**
 * A transient object-store failure: a connection error, a timeout, or a server-side error.
 * The worker classifies these as recoverable, so the job is retried. Permanent failures, such as a
 * missing object or a refused overwrite, stay plain {@link RuntimeException}s.
 */
public class RecoverableObjectStoreException extends RuntimeException {

    public RecoverableObjectStoreException(String message, Throwable cause) {
        super(message, cause);
    }
}
