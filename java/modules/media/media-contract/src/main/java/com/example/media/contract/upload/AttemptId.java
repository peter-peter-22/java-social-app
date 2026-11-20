package com.example.media.contract.upload;

import java.util.Objects;
import java.util.UUID;

/** The identity of one upload attempt. Completion is idempotent on this id. */
public record AttemptId(UUID value) {
    public AttemptId {
        Objects.requireNonNull(value, "value");
    }
}