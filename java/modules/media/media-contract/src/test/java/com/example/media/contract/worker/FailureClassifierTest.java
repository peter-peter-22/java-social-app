package com.example.media.contract.worker;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FailureClassifierTest {

    @Test
    void recoversMarkedFailures() {
        assertThat(FailureClassifier.isRecoverable(new RecoverableException("reset", new RuntimeException("reset"))))
                .isTrue();
    }

    @Test
    void findsTheMarkThroughTheCauseChain() {
        // A wrapped transient failure is found through the cause chain.
        assertThat(FailureClassifier.isRecoverable(new IllegalStateException(
                "transform failed", new RecoverableException("down", new RuntimeException("down")))))
                .isTrue();
    }

    @Test
    void doesNotRecoverUnmarkedFailures() {
        assertThat(FailureClassifier.isRecoverable(new IllegalStateException("boom"))).isFalse();
        assertThat(FailureClassifier.isRecoverable(new IllegalArgumentException("bad command"))).isFalse();
        assertThat(FailureClassifier.isRecoverable(new UnsupportedOperationException())).isFalse();
    }
}