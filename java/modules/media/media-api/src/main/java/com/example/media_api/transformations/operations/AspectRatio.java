package com.example.media_api.transformations.operations;

import org.jetbrains.annotations.NotNull;

public record AspectRatio(
        int width,
        int height,
        @NotNull Mode mode
) {
    public enum Mode {
        FILL,
        CONTAIN
    }
}
