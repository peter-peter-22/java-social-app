package com.example.media.contract.upload;

import org.jspecify.annotations.Nullable;

import java.util.Objects;

/**
 * The coordination record of one upload key: which attempt is currently allowed to finish the
 * upload, and which upload (if any) already finished it.
 * Its only job is to enforce "one final upload per key, decided by one active attempt".
 */
public record UploadProcess(UploadKey key, @Nullable AttemptId activeAttemptId, @Nullable UploadId uploadId) {
    public UploadProcess {
        Objects.requireNonNull(key, "key");
    }

    /** Whether the process already has its final upload. A finished process accepts no new attempt. */
    public boolean finished() {
        return uploadId != null;
    }
}