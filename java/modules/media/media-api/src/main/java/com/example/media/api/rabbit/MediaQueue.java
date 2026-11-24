package com.example.media.api.rabbit;

/**
 * The RabbitMQ topology of the media service. The image worker declares the same names, so the
 * topology cannot drift between the API and the worker.
 */
public final class MediaQueue {

    /** The commands from the API to the workers. */
    public static final String EXCHANGE = "media.worker.exchange";
    public static final String IMAGE_TRANSFORM = "media.worker.image-transform";
    public static final String COMMAND_ROUTING_KEY = "image.transform";

    /** The attempts a worker gave up on, dead-lettered for the API to mark failed. */
    public static final String DEAD_LETTER_EXCHANGE = "media.worker.dead-letter.exchange";
    public static final String FAILED = "media.worker.image-transform.failed";
    public static final String FAILED_ROUTING_KEY = "image-transform.failed";

    private MediaQueue() {
    }
}
