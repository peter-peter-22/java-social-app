package com.example.media.contract.asset;

/** The recorded dimensions of an image. */
public record ImageMetadata(int width, int height) {
    public ImageMetadata {
        if (width <= 0) throw new IllegalArgumentException("width must be positive: " + width);
        if (height <= 0) throw new IllegalArgumentException("height must be positive: " + height);
    }
}