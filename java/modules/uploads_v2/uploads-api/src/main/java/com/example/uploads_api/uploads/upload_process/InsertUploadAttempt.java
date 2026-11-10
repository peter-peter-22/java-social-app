package com.example.uploads_api.uploads.upload_process;

import lombok.Builder;
import lombok.Getter;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.UUID;

@NullMarked
@Getter
@Builder
public final class InsertUploadAttempt {
    private final String uploadKey;
    private final @Nullable List<String> eagerTransformationNames;
    private final @Nullable String incomingTransformationName;
    private final UUID originalAssetId;
}
