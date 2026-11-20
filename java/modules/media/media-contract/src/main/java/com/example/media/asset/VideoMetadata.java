package com.example.media.asset;

import org.jspecify.annotations.NullMarked;

/** Kept separate from image metadata because a video worker is a later deployable. */
@NullMarked
public record VideoMetadata(int width, int height, int durationMilliseconds) {
    public VideoMetadata {
        if (width <= 0 || height <= 0 || durationMilliseconds <= 0) {
            throw new IllegalArgumentException("Video metadata values must be positive");
        }
    }
}
