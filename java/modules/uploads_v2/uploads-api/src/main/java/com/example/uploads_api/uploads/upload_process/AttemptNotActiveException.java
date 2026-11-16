package com.example.uploads_api.uploads.upload_process;

public class AttemptNotActiveException extends RuntimeException {
    public AttemptNotActiveException(String message) {
        super(message);
    }
}
