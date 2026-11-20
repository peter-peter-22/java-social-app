package com.example.media.asset;

import org.jspecify.annotations.NullMarked;

@NullMarked
public record ImageMetadata(int width, int height) {
    public ImageMetadata {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Image dimensions must be positive");
        }
    }
}
