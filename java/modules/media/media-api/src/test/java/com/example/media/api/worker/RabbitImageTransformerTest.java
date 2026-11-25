package com.example.media.api.worker;

import com.example.media.contract.asset.AssetPlacement;
import com.example.media.contract.object.ObjectRef;
import com.example.media.contract.transformation.ImageEncoding;
import com.example.media.contract.transformation.ImageTransformationSpec;
import com.example.media.contract.upload.AttemptId;
import com.example.media.contract.worker.ImageTransformCommand;
import com.example.media.contract.worker.PlannedImageOutput;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * The async adapter publishes the command to the queue in the contract's JSON shape and returns
 * immediately.
 */
class RabbitImageTransformerTest {

    private final RabbitTemplate rabbitTemplate = mock();
    private final RabbitImageTransformer adapter =
            new RabbitImageTransformer(rabbitTemplate, "media.worker.exchange", "image.transform");

    private static final AttemptId ATTEMPT_ID = new AttemptId(UUID.randomUUID());
    private static final ImageTransformCommand COMMAND = new ImageTransformCommand(
            ATTEMPT_ID,
            new ObjectRef("eu-west", "uploads", "3f2b/original_jpg"),
            List.of(new PlannedImageOutput(
                    null,
                    new AssetPlacement("eu-west", "uploads"),
                    new ImageTransformationSpec(null, null, null, new ImageEncoding.Webp(50)))));

    @Test
    void publishesTheCommandToTheWorkerQueue() {
        adapter.transformAsync(COMMAND);

        verify(rabbitTemplate).convertAndSend("media.worker.exchange", "image.transform", COMMAND);
    }
}
