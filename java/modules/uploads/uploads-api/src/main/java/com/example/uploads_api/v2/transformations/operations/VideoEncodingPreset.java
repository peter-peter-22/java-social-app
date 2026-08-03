package com.example.uploads_api.v2.transformations.operations;

/**
 * The selected profile affects the encoding speed and compression quality, but not the target quality.
 * (slow or medium is the standard)
 */
public enum VideoEncodingPreset {
    ultrafast,
    superfast,
    veryfast,
    medium,
    slow,
    veryslow
}
