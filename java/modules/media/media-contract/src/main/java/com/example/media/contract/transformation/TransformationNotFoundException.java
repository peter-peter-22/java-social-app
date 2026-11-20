package com.example.media.contract.transformation;

/** No named transformation exists under the requested name. */
public class TransformationNotFoundException extends RuntimeException {
    public TransformationNotFoundException(String message) {
        super(message);
    }
}