package com.example.media.registry;

import com.example.media.transformation.ImageTransformation;
import com.example.media.transformation.NamedTransformation;
import com.example.media.transformation.TransformationNotFound;
import org.jspecify.annotations.NullMarked;

import java.util.Collection;
import java.util.List;

/** Named image transformations. {@link #getAll} fails when any requested name is missing. */
@NullMarked
public interface TransformationCatalog {
    NamedTransformation get(String name) throws TransformationNotFound;

    List<NamedTransformation> getAll(Collection<String> names) throws TransformationNotFound;

    void save(String name, ImageTransformation operations);
}
