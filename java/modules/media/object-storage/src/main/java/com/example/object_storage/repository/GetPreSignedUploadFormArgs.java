package com.example.object_storage.repository;

import lombok.Builder;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.temporal.TemporalUnit;

@Builder
@Getter
public class GetPreSignedUploadFormArgs {
    public record ContentLengthRange(int minBytes, int maxBytes) {}

    private final @NotNull String bucket;
    private final @Nullable String objectPath;
    private final @NotNull Integer expiration;
    private final @NotNull TemporalUnit timeUnit;
    private final @Nullable String contentType;
    private final @Nullable ContentLengthRange contentLengthRange;
}
