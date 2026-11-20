package com.example.media.contract.upload;

/** The upload process is already finished; no new attempt may become active on it. */
public class UploadAlreadyFinishedException extends RuntimeException {
    public UploadAlreadyFinishedException(String message) {
        super(message);
    }
}