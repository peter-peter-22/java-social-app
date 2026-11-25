package com.example.media.api.transformation;

import com.example.media.contract.port.TransformationCatalog;
import com.example.media.contract.transformation.NamedTransformation;
import com.example.media.contract.transformation.NewNamedTransformation;
import com.example.media.contract.transformation.TransformationSpec;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;

/**
 * The admin's define-transformations use case: registers a named transformation or records a new
 * version of it. Bad input (a missing name or spec) is an {@link IllegalArgumentException}, which
 * the web layer delivers as 400.
 */
@Service
@RequiredArgsConstructor
@NullMarked
public class RegisterTransformation {
    private final TransformationCatalog transformationCatalog;

    public NamedTransformation register(@Nullable String name, @Nullable TransformationSpec spec) {
        if (name == null)
            throw new IllegalArgumentException("the transformation name is required");
        if (spec == null)
            throw new IllegalArgumentException("the transformation spec is required");
        return transformationCatalog.register(new NewNamedTransformation(name, spec));
    }
}
