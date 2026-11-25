package com.example.media.api.security;

/**
 * A read of a private upload that its download credential does not authorize: the token is missing,
 * unknown, tampered, expired, or bound to another upload key or version. The web layer delivers it
 * as 403, never as the 400 of a malformed request.
 */
public class DownloadDeniedException extends RuntimeException {
    public DownloadDeniedException(String message) {
        super(message);
    }
}