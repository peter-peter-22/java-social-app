package com.example.media.transformation;

/** A requested transformation name is not in the catalog. */
public final class TransformationNotFound extends Exception {
    public TransformationNotFound(String message) {
        super(message);
    }
}
