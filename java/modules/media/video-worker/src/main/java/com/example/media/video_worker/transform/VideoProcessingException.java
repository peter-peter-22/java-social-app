package com.example.media.video_worker.transform;

/**
 * A failed probe or encoding run. The worker's use case decides whether the failure is recoverable;
 * the original ffmpeg exception is kept as the cause so its stderr is not lost.
 */
public class VideoProcessingException extends RuntimeException {

    public VideoProcessingException(String message) {
        super(message);
    }

    public VideoProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
