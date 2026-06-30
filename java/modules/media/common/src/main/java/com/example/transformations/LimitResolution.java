package com.example.transformations;

import org.jetbrains.annotations.NotNull;

public record LimitResolution(
        int pixels,
        @NotNull Mode mode
) {
    public enum Mode {
        KEEP_ASPECT_RATION
    }
}
