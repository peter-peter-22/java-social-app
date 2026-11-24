package com.example.media.imageworker.rabbit;

import com.example.object_storage.RecoverableObjectStoreException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class WorkerFailureClassifierTest {

    private final WorkerFailureClassifier classifier = new WorkerFailureClassifier();

    @Test
    void recoversTransportAndServerFailures() {
        assertThat(classifier.isRecoverable(new ResourceAccessException("reset", new IOException("reset"))))
                .isTrue();
        assertThat(classifier.isRecoverable(new HttpServerErrorException(HttpStatus.BAD_GATEWAY))).isTrue();
        assertThat(classifier.isRecoverable(new RecoverableObjectStoreException("down", new IOException("down"))))
                .isTrue();
        // A wrapped store failure is found through the cause chain.
        assertThat(classifier.isRecoverable(new IllegalStateException(
                "transform failed", new RecoverableObjectStoreException("down", new IOException("down")))))
                .isTrue();
    }

    @Test
    void doesNotRecoverPermanentFailures() {
        assertThat(classifier.isRecoverable(new IllegalStateException("boom"))).isFalse();
        assertThat(classifier.isRecoverable(new IllegalArgumentException("bad command"))).isFalse();
        assertThat(classifier.isRecoverable(new UnsupportedOperationException())).isFalse();
    }
}
