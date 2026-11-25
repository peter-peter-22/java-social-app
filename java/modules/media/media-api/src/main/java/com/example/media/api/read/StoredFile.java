package com.example.media.api.read;

import com.example.media.contract.asset.Asset;
import com.example.media.contract.port.ObjectStore;
import com.example.media.contract.upload.UploadVisibility;

import java.io.InputStream;
import java.util.Objects;

/**
 * The stored file a read use case returns for delivery: the content type to answer with, the file
 * name to suggest, the content stream, and the upload's visibility so the controller can choose the
 * cache header. The controller maps it to HTTP.
 */
public record StoredFile(String contentType, String fileName, InputStream content, UploadVisibility visibility) {
    public StoredFile {
        Objects.requireNonNull(contentType, "contentType");
        Objects.requireNonNull(fileName, "fileName");
        Objects.requireNonNull(content, "content");
        Objects.requireNonNull(visibility, "visibility");
    }

    /** Opens the entry file of a stored asset for delivery. */
    static StoredFile open(Asset asset, ObjectStore objectStore, UploadVisibility visibility) {
        return new StoredFile(
                asset.contentType(),
                asset.entryPoint(),
                objectStore.get(asset.prefix().resolve(asset.entryPoint())),
                visibility);
    }
}