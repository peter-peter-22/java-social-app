package com.example.media_api.uploads;

import org.jetbrains.annotations.NotNull;

public record ObjectLocation(
        @NotNull String path,
        @NotNull String bucket
) {
}
