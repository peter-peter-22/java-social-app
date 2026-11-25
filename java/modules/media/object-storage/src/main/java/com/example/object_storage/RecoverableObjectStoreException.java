package com.example.object_storage;

import com.example.media.contract.worker.RecoverableException;

/**
 * A transient object-store failure: a connection error, a timeout, or a server-side error.
 * It extends the contract's recoverable failure, so the shared retry conditions
 * ({@code com.example.media.contract.worker.FailureClassifier}) retry it like the transient
 * failures of the other adapters. Permanent failures, such as a missing object or a refused
 * overwrite, stay plain {@link RuntimeException}s.
 */
public class RecoverableObjectStoreException extends RecoverableException {

    public RecoverableObjectStoreException(String message, Throwable cause) {
        super(message, cause);
    }
}
