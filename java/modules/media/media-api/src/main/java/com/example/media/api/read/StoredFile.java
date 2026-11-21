package com.example.media.api.read;

import java.io.InputStream;
import java.util.Objects;

/**
 * The stored file a read use case returns for delivery: the content type to answer with, the file
 * name to suggest, and the content stream. The controller maps it to HTTP.
 */
public record StoredFile(String contentType, String fileName, InputStream content) {
    public StoredFile {
        Objects.requireNonNull(contentType, "contentType");
        Objects.requireNonNull(fileName, "fileName");
        Objects.requireNonNull(content, "content");
    }
}