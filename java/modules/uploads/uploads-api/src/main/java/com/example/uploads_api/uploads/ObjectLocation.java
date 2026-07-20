package com.example.uploads_api.uploads;

import lombok.Builder;
import org.jetbrains.annotations.NotNull;

@Builder
public record ObjectLocation(
        @NotNull String key,
        @NotNull String bucket
) {
}
