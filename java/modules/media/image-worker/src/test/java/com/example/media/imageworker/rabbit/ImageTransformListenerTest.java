package com.example.media.imageworker.rabbit;

import com.example.media.contract.asset.AssetPlacement;
import com.example.media.contract.object.ObjectRef;
import com.example.media.contract.transformation.ImageEncoding;
import com.example.media.contract.transformation.ImageTransformationSpec;
import com.example.media.contract.upload.AttemptId;
import com.example.media.contract.worker.ImageTransformCommand;
import com.example.media.contract.worker.ImageTransformResult;
import com.example.media.contract.worker.PlannedImageOutput;
import com.example.media.imageworker.completion.ApiCompletionClient;
import com.example.media.imageworker.transform.TransformImage;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The listener runs the same transform use case as the blocking endpoint and reports the result to
 * the API. A failure on either step throws, so the container retries or dead-letters the message.
 */
class ImageTransformListenerTest {

    private final TransformImage transformImage = mock();
    private final ApiCompletionClient completionClient = mock();
    private final ImageTransformListener listener = new ImageTransformListener(transformImage, completionClient);

    private static final AttemptId ATTEMPT_ID = new AttemptId(UUID.randomUUID());
    private static final ImageTransformCommand COMMAND = new ImageTransformCommand(
            ATTEMPT_ID,
            new ObjectRef("eu-west", "uploads", "3f2b/original_jpg"),
            List.of(new PlannedImageOutput(
                    null,
                    new AssetPlacement("eu-west", "uploads"),
                    new ImageTransformationSpec(null, null, null, new ImageEncoding.Webp(50)))));

    @Test
    void transformsAndCompletes() {
        var result = new ImageTransformResult(ATTEMPT_ID, List.of(), null);
        when(transformImage.transform(COMMAND)).thenReturn(result);

        listener.onCommand(COMMAND);

        verify(transformImage).transform(COMMAND);
        verify(completionClient).complete(result);
    }

    @Test
    void aTransformFailurePropagates() {
        when(transformImage.transform(COMMAND)).thenThrow(new IllegalStateException("boom"));

        assertThatThrownBy(() -> listener.onCommand(COMMAND)).isInstanceOf(IllegalStateException.class);

        verify(completionClient, never()).complete(any());
    }

    @Test
    void aCompletionFailurePropagates() {
        var result = new ImageTransformResult(ATTEMPT_ID, List.of(), null);
        when(transformImage.transform(COMMAND)).thenReturn(result);
        doThrow(new IllegalStateException("api down")).when(completionClient).complete(any());

        assertThatThrownBy(() -> listener.onCommand(COMMAND)).isInstanceOf(IllegalStateException.class);
    }
}
