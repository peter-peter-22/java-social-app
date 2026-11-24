package com.example.media.imageworker.rabbit;

import com.example.media.contract.port.FailureClassifier;
import com.example.object_storage.RecoverableObjectStoreException;
import org.jspecify.annotations.NullMarked;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

/**
 * The recoverability of an async image transform: object-store transport failures
 * ({@link RecoverableObjectStoreException}) and the completion call's transport and server errors
 * ({@link ResourceAccessException}, {@link HttpServerErrorException}) are retried; everything else
 * is permanent and cancels the retries.
 */
@NullMarked
public final class WorkerFailureClassifier implements FailureClassifier {

    @Override
    public boolean isRecoverable(Throwable failure) {
        for (Throwable cause = failure; cause != null; cause = cause.getCause()) {
            if (cause instanceof ResourceAccessException
                    || cause instanceof HttpServerErrorException
                    || cause instanceof RecoverableObjectStoreException) {
                return true;
            }
        }
        return false;
    }
}
