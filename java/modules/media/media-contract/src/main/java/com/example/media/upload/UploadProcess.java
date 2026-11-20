package com.example.media.upload;

import com.example.media.identity.AttemptId;
import com.example.media.identity.UploadId;
import com.example.media.identity.UploadKey;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

/** Lock row for one upload key. The upload id is set only after publication. */
@NullMarked
public record UploadProcess(
        UploadKey uploadKey,
        @Nullable AttemptId activeAttemptId,
        @Nullable UploadId uploadId
) {
    public UploadProcess {
        Objects.requireNonNull(uploadKey, "uploadKey");
    }

    public boolean published() {
        return uploadId != null;
    }
}
