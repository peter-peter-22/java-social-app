package com.example.media.registry;

/** The attempt is not the active attempt of its upload process. */
public final class AttemptNotActive extends Exception {
    public AttemptNotActive(String message) {
        super(message);
    }
}
