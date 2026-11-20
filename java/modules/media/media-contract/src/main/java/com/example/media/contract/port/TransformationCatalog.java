package com.example.media.contract.port;

import com.example.media.contract.transformation.NamedTransformation;
import com.example.media.contract.transformation.NewNamedTransformation;
import com.example.media.contract.transformation.TransformationNotFoundException;

import java.util.Collection;
import java.util.List;

/**
 * The named transformation catalog.
 * Resolving produces the current version of a name; produced variants pin that version, so later
 * updates never change existing results.
 */
public interface TransformationCatalog {

    /** Returns the named transformation with its current version. */
    NamedTransformation resolve(String name) throws TransformationNotFoundException;

    /**
     * Resolves all names or none of them.
     *
     * @throws TransformationNotFoundException when any of the names is unknown.
     */
    List<NamedTransformation> resolveAll(Collection<String> names) throws TransformationNotFoundException;

    /**
     * Registers the named transformation, creating it if absent, records the spec as a version,
     * and makes that version current. A spec identical to a recorded version is reused instead of
     * duplicated.
     */
    NamedTransformation register(NewNamedTransformation insert);
}