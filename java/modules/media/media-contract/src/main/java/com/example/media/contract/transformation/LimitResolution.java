package com.example.media.contract.transformation;

/** Limits the resolution on one axis. */
public record LimitResolution(int pixels, Mode mode) {
    public LimitResolution {
        if (pixels <= 0) throw new IllegalArgumentException("pixels must be positive: " + pixels);
        if (mode == null) throw new NullPointerException("mode");
    }

    public enum Mode {
        KEEP_ASPECT_RATIO
    }
}