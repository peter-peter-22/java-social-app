package com.example.media.contract.worker;

/**
 * The RabbitMQ topology names of the media service. The API and the workers share this one
 * definition, so the topology cannot drift between them. Each deployable declares the topology
 * entities it uses from these names.
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
