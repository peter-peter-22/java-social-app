package com.example.media.api.upload;

import com.example.media.contract.port.UploadRegistry;
import com.example.media.contract.upload.UploadAttemptStatus;
import com.example.media.contract.upload.UploadKey;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Service;

/**
 * Reads the state of the active upload attempt over HTTP, so a client that queued an async upload
 * can poll for its completion without touching the registry internals. A key without an attempt has
 * no status and is a {@link UploadNotFoundException}. The attempt keeps its status until the
 * periodic cleanup; {@code SUCCESS} means the upload is finalized and readable, {@code FAILED}
 * means the attempt is dead and the upload will never arrive.
 */
@Service
@RequiredArgsConstructor
@NullMarked
public class ReadUploadStatus {
    private final UploadRegistry uploadRegistry;

    /** The status of the key's active attempt. */
    public record Result(UploadKey key, UploadAttemptStatus status) {
    }

    public Result read(UploadKey key) {
        var process = uploadRegistry.findProcess(key)
                .orElseThrow(() -> new UploadNotFoundException("no upload process: " + key.value()));
        if (process.activeAttemptId() == null)
            throw new UploadNotFoundException("no upload attempt: " + key.value());
        var attempt = uploadRegistry.findAttempt(process.activeAttemptId())
                .orElseThrow(() -> new UploadNotFoundException("no upload attempt: " + key.value()));
        return new Result(key, attempt.status());
    }
}
