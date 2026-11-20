package com.example.media.transformation;

import org.jspecify.annotations.NullMarked;

import java.util.Objects;

@NullMarked
public record AspectRatio(int width, int height, Mode mode) {
    public AspectRatio {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Aspect ratio must be positive");
        }
        Objects.requireNonNull(mode, "mode");
    }

    public enum Mode {
        FILL,
        CONTAIN
    }
}
