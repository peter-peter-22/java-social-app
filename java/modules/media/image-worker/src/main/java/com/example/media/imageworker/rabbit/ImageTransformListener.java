package com.example.media.imageworker.rabbit;

import com.example.media.contract.worker.ImageTransformCommand;
import com.example.media.contract.worker.MediaQueue;
import com.example.media.imageworker.completion.ApiCompletionClient;
import com.example.media.imageworker.transform.TransformImage;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * The async entry point of the worker: it runs the same transform use case as the blocking HTTP
 * endpoint, then calls the API completion endpoint with the result. The message is acked when the
 * listener returns, so a failure on either step throws and the container retries or dead-letters
 * the message; a redelivery re-runs the transform with fresh result asset ids.
 */
@Component
@RequiredArgsConstructor
@NullMarked
public class ImageTransformListener {
    private final TransformImage transformImage;
    private final ApiCompletionClient completionClient;

    @RabbitListener(queues = MediaQueue.IMAGE_TRANSFORM)
    public void onCommand(ImageTransformCommand command) {
        var result = transformImage.transform(command);
        completionClient.complete(result);
    }
}
