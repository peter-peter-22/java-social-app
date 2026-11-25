package com.example.media.api.web;

import com.example.media.api.read.ReadCacheProperties;
import com.example.media.api.read.ReadOriginal;
import com.example.media.api.read.ReadVariant;
import com.example.media.api.read.StoredFile;
import com.example.media.api.security.DownloadCredential;
import com.example.media.contract.upload.UploadKey;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * The client's image read endpoints. They only map HTTP: the path becomes the use case's request,
 * and the stored file becomes a streamed response. The extension in the path is cosmetic; it makes
 * browsers display the image instead of downloading it when the URL is opened directly.
 *
 * <p>The {@code v}, {@code e}, {@code s}, and {@code k} query parameters carry the cache version
 * and the download credential. A public read ignores them and answers with the immutable public
 * cache header; a private read requires a valid credential and answers with the short private cache
 * header. The use case enforces the visibility and version rules. Every media response carries
 * {@code Referrer-Policy: no-referrer}, so a token-bearing URL never leaks through a cross-origin
 * referrer.</p>
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@NullMarked
public class ImageReadController {
    /** The header that keeps token-bearing read URLs out of cross-origin referrers. */
    private static final String REFERRER_POLICY = "Referrer-Policy";

    private final ReadOriginal readOriginal;
    private final ReadVariant readVariant;
    private final ReadCacheProperties cacheProperties;

    @GetMapping(path = "/get/image/{uploadKey}.{extension}")
    ResponseEntity<InputStreamResource> readOriginal(
            @PathVariable String uploadKey,
            @PathVariable String extension,
            @RequestParam(name = "v", required = false) @Nullable String version,
            @RequestParam(name = "e", required = false) @Nullable Long expires,
            @RequestParam(name = "s", required = false) @Nullable String signature,
            @RequestParam(name = "k", required = false) @Nullable String keyId
    ) {
        return toResponse(readOriginal.read(
                new UploadKey(uploadKey), version, credential(expires, signature, keyId)));
    }

    @GetMapping(path = "/get/image/{uploadKey}/{transformation}.{extension}")
    ResponseEntity<InputStreamResource> readVariant(
            @PathVariable String uploadKey,
            @PathVariable String transformation,
            @PathVariable String extension,
            @RequestParam(name = "v", required = false) @Nullable String version,
            @RequestParam(name = "e", required = false) @Nullable Long expires,
            @RequestParam(name = "s", required = false) @Nullable String signature,
            @RequestParam(name = "k", required = false) @Nullable String keyId
    ) {
        return toResponse(readVariant.read(
                new UploadKey(uploadKey), transformation, version, credential(expires, signature, keyId)));
    }

    /** Builds the credential from the {@code e}, {@code s}, {@code k} parameters; null unless all are present. */
    private static @Nullable DownloadCredential credential(
            @Nullable Long expires,
            @Nullable String signature,
            @Nullable String keyId
    ) {
        if (expires == null || signature == null || keyId == null)
            return null;
        return new DownloadCredential(expires, signature, keyId);
    }

    private ResponseEntity<InputStreamResource> toResponse(StoredFile file) {
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(file.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"%s\"".formatted(file.fileName()))
                .header(HttpHeaders.CACHE_CONTROL, cacheProperties.forVisibility(file.visibility()))
                .header(REFERRER_POLICY, "no-referrer")
                .body(new InputStreamResource(file.content()));
    }
}
