package com.example.uploads_api.uploads.upload_process;

public class UploadAlreadyExistsException extends RuntimeException {
    public UploadAlreadyExistsException(String message) {
        super(message);
    }
}
