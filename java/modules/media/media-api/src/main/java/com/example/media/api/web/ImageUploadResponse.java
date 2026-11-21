package com.example.media.api.web;

import java.time.Instant;

/** The answer of a completed image upload: which upload was finalized and when. */
public record ImageUploadResponse(String uploadKey, Instant createdAt) {
}