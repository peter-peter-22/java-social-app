package com.example.uploads_api.uploads.upload_process;

import lombok.Builder;
import lombok.Getter;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

/** Atomic storage for which upload attempt is currently active and if the upload finished successfully.  */
@NullMarked
@Getter
@Builder
public final class UploadProcess {
    /** The upload key serves as the unique ID of the upload process. */
    private final String uploadKey;
    private final UUID activeAttemptId;
    private final @Nullable UUID uploadId;
}
