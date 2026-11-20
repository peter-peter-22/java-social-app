package com.example.media.contract.transformation;

import java.util.Objects;

/** The data needed to register or update a named transformation. */
public record NewNamedTransformation(String name, TransformationSpec spec) {
    public NewNamedTransformation {
        Objects.requireNonNull(name, "name");
        if (name.isBlank()) throw new IllegalArgumentException("name must not be blank");
        if (name.length() > 50)
            throw new IllegalArgumentException("name is longer than 50 characters: " + name);
        Objects.requireNonNull(spec, "spec");
    }
}