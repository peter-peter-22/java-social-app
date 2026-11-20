package com.example.media.contract.transformation;

/** Crops or pads the image to the requested aspect ratio. */
public record AspectRatio(int width, int height, Mode mode) {
    public AspectRatio {
        if (width <= 0) throw new IllegalArgumentException("width must be positive: " + width);
        if (height <= 0) throw new IllegalArgumentException("height must be positive: " + height);
        if (mode == null) throw new NullPointerException("mode");
    }

    public enum Mode {
        FILL,
        CONTAIN
    }
}