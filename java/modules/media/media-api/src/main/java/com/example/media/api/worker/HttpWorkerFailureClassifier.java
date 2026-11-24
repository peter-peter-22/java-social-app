package com.example.media.api.worker;

import com.example.media.contract.port.FailureClassifier;
import org.jspecify.annotations.NullMarked;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

/**
 * The recoverability of a blocking worker HTTP call: transport failures
 * ({@link ResourceAccessException}) and worker server errors ({@link HttpServerErrorException}) are
 * retried; client errors and anything else are permanent. This is the reference classification the
 * async path must share.
 */
@NullMarked
public final class HttpWorkerFailureClassifier implements FailureClassifier {

    @Override
    public boolean isRecoverable(Throwable failure) {
        for (Throwable cause = failure; cause != null; cause = cause.getCause()) {
            if (cause instanceof ResourceAccessException || cause instanceof HttpServerErrorException)
                return true;
        }
        return false;
    }
}
