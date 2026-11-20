package com.example.media.contract.asset;

/** The recorded dimensions and duration of a video. */
public record VideoMetadata(int width, int height, long durationMs) {
    public VideoMetadata {
        if (width <= 0) throw new IllegalArgumentException("width must be positive: " + width);
        if (height <= 0) throw new IllegalArgumentException("height must be positive: " + height);
        if (durationMs <= 0) throw new IllegalArgumentException("durationMs must be positive: " + durationMs);
    }
}