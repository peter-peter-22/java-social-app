package com.example.media.contract.upload;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * The client-chosen key of an upload. It identifies the upload process and its final upload.
 * The length limit follows the registry column; do not raise it without a schema change.
 *
 * <p>The charset is restricted to {@code [A-Za-z0-9_-]}: the key is a URL path segment (the nginx
 * edge takes the first segment after {@code /cdn/} as the upload key) and part of the signed
 * canonical string, so a path separator, a dot, or a space would make the path and the signature
 * ambiguous. Server-generated keys are unpadded base64url and fit.</p>
 */
public record UploadKey(String value) {
    private static final Pattern ALLOWED = Pattern.compile("[A-Za-z0-9_-]+");

    public UploadKey {
        Objects.requireNonNull(value, "value");
        if (value.isBlank())
            throw new IllegalArgumentException("upload key must not be blank");
        if (value.length() > 100)
            throw new IllegalArgumentException("upload key is longer than 100 characters: " + value);
        if (!ALLOWED.matcher(value).matches())
            throw new IllegalArgumentException(
                    "upload key may only contain A-Z, a-z, 0-9, '_' and '-': " + value);
    }
}