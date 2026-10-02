package com.example.uploads_service.api.transformations.operations;

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
