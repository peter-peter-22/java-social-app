package com.example.media.api.web;

import org.jspecify.annotations.Nullable;

import java.time.Instant;

/**
 * The answer of an image upload: which upload was accepted, when, and whether it is processed
 * synchronously. {@code async} true means the eager and incoming transformations run on the queue
 * and the upload does not exist yet, so the client cannot read it immediately. {@code version} is
 * the read cache version of the finalized upload; it is null for an async acceptance because the
 * upload does not exist yet.
 */
public record ImageUploadResponse(
        String uploadKey,
        Instant createdAt,
        boolean async,
        @Nullable String version
) {
}