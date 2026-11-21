package com.example.media.api.read;

/**
 * The requested variant does not exist: the upload has no variant under the requested
 * transformation name. Delivered as 404.
 */
public class VariantNotFoundException extends RuntimeException {
    public VariantNotFoundException(String message) {
        super(message);
    }
}