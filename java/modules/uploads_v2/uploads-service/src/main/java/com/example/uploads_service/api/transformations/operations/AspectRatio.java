package com.example.uploads_service.api.transformations.operations;

import org.jspecify.annotations.NonNull;

public record AspectRatio(
        int width,
        int height,
        @NonNull Mode mode
) {
    public enum Mode {
        FILL,
        CONTAIN
    }
}
