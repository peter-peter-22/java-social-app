package com.example.media.api.read;

import com.example.media.contract.upload.UploadVisibility;
import org.jspecify.annotations.NullMarked;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * The read delivery settings ({@code media.read}): the {@code Cache-Control} values the read
 * endpoints answer with.
 *
 * <p>Media responses stay cacheable. A public read is immutable because its URL carries the cache
 * version, so a replaced upload produces a new URL. A private read is cacheable by the browser for a
 * short window; it must never be marked {@code no-store}, because that would disable caching without
 * adding security. Only credential-issuance responses are {@code no-store}.</p>
 */
@ConfigurationProperties(prefix = "media.read")
@NullMarked
public record ReadCacheProperties(
        @DefaultValue("public, max-age=31536000, immutable") String publicCacheControl,
        @DefaultValue("private, max-age=300") String privateCacheControl
) {
    /** The cache header for a read of the given visibility. */
    public String forVisibility(UploadVisibility visibility) {
        return visibility == UploadVisibility.PUBLIC ? publicCacheControl : privateCacheControl;
    }
}