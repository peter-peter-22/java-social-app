package com.example.object_storage.repository;

import com.example.uploads_api.uploads.ObjectLocation;
import lombok.Builder;
import lombok.Getter;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.time.temporal.TemporalUnit;

@Builder
@Getter
public class GetPreSignedUploadFormArgs {
    public record ContentLengthRange(int minBytes, int maxBytes) {}

    private final @NonNull ObjectLocation location;
    private final @NonNull Integer expiration;
    private final @NonNull TemporalUnit timeUnit;
    private final @Nullable String contentType;
    private final @Nullable ContentLengthRange contentLengthRange;
}
