package com.example.uploads_api.uploads;

import lombok.Builder;
import org.jspecify.annotations.NonNull;

@Builder
public record ObjectLocation(
        @NonNull String key,
        @NonNull String bucket
) {
}
