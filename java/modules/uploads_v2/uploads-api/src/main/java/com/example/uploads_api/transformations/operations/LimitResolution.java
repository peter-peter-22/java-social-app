package com.example.uploads_api.transformations.operations;

import org.jspecify.annotations.NonNull;

public record LimitResolution(
        int pixels,
        @NonNull Mode mode
) {
    public enum Mode {
        KEEP_ASPECT_RATIO
    }
}
