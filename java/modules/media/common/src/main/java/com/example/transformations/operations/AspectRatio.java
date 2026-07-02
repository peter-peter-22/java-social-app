package com.example.transformations.operations;

import org.jetbrains.annotations.NotNull;

public record AspectRatio(
        int width,
        int height,
        @NotNull Mode mode
) {
    public static enum Mode {
        FILL
    }
}
