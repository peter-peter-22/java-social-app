package com.example.media.contract.port;

import com.example.media.contract.asset.AssetPlacement;
import com.example.media.contract.object.ObjectLifetime;
import com.example.media.contract.object.ObjectRef;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;

/**
 * The object store, behind a plain vocabulary.
 * The adapter keeps the vendor types, regions, tags, and reconciliation; nothing of that leaks
 * into the application services. {@link ObjectLifetime} is the only lifecycle knob: the adapter
 * maps it to its expiration tags.
 */
public interface ObjectStore {

    /**
     * The placement values recorded for new assets: the home region this store writes from and the
     * bucket that holds the uploads.
     */
    AssetPlacement homePlacement();

    /** Writes an object from a stream. The object must not exist yet; overwrites are not allowed. */
    void put(ObjectRef ref, InputStream data, long bytes, String contentType, ObjectLifetime lifetime);

    /** Writes an object from a file. Chooses the upload strategy inside the adapter. */
    void put(ObjectRef ref, Path source, String contentType, ObjectLifetime lifetime);

    /** Opens an object for reading. */
    InputStream get(ObjectRef ref);

    /** Downloads an object into a local file. Used by workers that transform from disk. */
    void download(ObjectRef ref, Path target);

    /** Sets the lifetime of every given object. Used at finalize to make results permanent. */
    void setLifetime(List<ObjectRef> refs, ObjectLifetime lifetime);

    /** Whether the object exists. Checked after the expiration is removed and before commit. */
    boolean exists(ObjectRef ref);

    void delete(ObjectRef ref);
}