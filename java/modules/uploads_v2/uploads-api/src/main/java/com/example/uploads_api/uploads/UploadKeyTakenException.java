package com.example.uploads_api.uploads;

public class UploadKeyTakenException extends RuntimeException {
    public UploadKeyTakenException(String message) {
        super(message);
    }
}
