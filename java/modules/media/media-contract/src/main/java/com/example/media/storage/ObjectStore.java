package com.example.media.storage;

import com.example.media.asset.AssetLocation;
import org.jspecify.annotations.NullMarked;

import java.io.InputStream;
import java.nio.file.Path;

/**
 * Object storage as the upload lifecycle sees it.
 * Temporary and permanent are lifecycle states. The adapter owns regions, tags, and reconciliation.
 * Callers may only address relative paths registered on the location.
 */
@NullMarked
public interface ObjectStore {
    ObjectPlacement placement();

    void writeTemporary(AssetLocation location, String relativePath, InputStream body, long contentLength, String contentType);

    void writeTemporaryFile(AssetLocation location, String relativePath, Path source, String contentType);

    /** The caller closes the returned stream. */
    InputStream read(AssetLocation location, String relativePath);

    void download(AssetLocation location, String relativePath, Path destination);

    /** Removes the expiration of every object registered on the location. */
    void makePermanent(AssetLocation location);

    /** False when any registered object is missing. */
    boolean allExist(AssetLocation location);
}
