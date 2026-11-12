package com.example.uploads_api.transformations;

/**
 * The selected profile affects the encoding speed and compression quality, but not the target quality.
 * (slow or medium is the standard)
 */
public enum VideoEncodingPreset {
    ULTRA_FAST,
    SUPER_FAST,
    VERY_FAST,
    MEDIUM,
    SLOW,
    VERY_SLOW
}
