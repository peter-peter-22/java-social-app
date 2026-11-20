package com.example.media.transformation;

import org.jspecify.annotations.NullMarked;

import java.util.Objects;

@NullMarked
public record LimitResolution(int pixels, Mode mode) {
    public LimitResolution {
        if (pixels <= 0) {
            throw new IllegalArgumentException("Pixel limit must be positive");
        }
        Objects.requireNonNull(mode, "mode");
    }

    public enum Mode {
        KEEP_ASPECT_RATIO
    }
}
