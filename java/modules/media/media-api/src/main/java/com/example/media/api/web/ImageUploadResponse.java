package com.example.media.api.web;

import java.time.Instant;

/**
 * The answer of an image upload: which upload was accepted, when, and whether it is processed
 * synchronously. {@code async} true means the eager and incoming transformations run on the queue
 * and the upload does not exist yet, so the client cannot read it immediately.
 */
public record ImageUploadResponse(String uploadKey, Instant createdAt, boolean async) {
}