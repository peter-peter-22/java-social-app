package com.example.media.api.rabbit;

import com.example.media.api.upload.FailImageUpload;
import com.example.media.contract.worker.ImageTransformCommand;
import com.example.media.contract.worker.MediaQueue;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Consumes the dead-lettered commands: a worker gave up on the attempt, so the API marks it failed.
 * The API owns the lifecycle; the worker has no database. Unparseable payloads are fatal and are
 * dropped by the container; a database failure requeues the message by default.
 */
@Component
@RequiredArgsConstructor
@NullMarked
public class ImageTransformFailedListener {
    private final FailImageUpload failImageUpload;

    @RabbitListener(queues = MediaQueue.FAILED)
    public void onFailed(ImageTransformCommand command) {
        failImageUpload.fail(command.attemptId());
    }
}
