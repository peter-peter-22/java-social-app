package com.example.image_transformer.webhook;

import com.example.image_transformer.task.ImageTransformationTask;
import com.example.uploads_api.transformations.webhook.WebhookCall;
import com.example.uploads_api.uploads.UploadId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static com.example.image_transformer.task.TestTaskCreator.createTask;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class WebhookServiceTest {
    private static final UploadId UPLOAD_ID = new UploadId(UUID.randomUUID());
    private static final String TRANSFORMATION_NAME = "thumbnail";

    @Mock
    private WebhookApi webhookApi;

    @Test
    void callsWebhookForLazyTask() {
        var service = new WebhookService(webhookApi);
        var task = taskWithLazy(true);

        service.handleCallback(task);

        var callCaptor = ArgumentCaptor.forClass(WebhookCall.class);
        verify(webhookApi).call(callCaptor.capture());
        assertThat(callCaptor.getValue())
                .isEqualTo(new WebhookCall(UPLOAD_ID, TRANSFORMATION_NAME));
    }

    @Test
    void doesNotCallWebhookForNonLazyTask() {
        var service = new WebhookService(webhookApi);

        service.handleCallback(taskWithLazy(false));

        verifyNoInteractions(webhookApi);
    }

    private static ImageTransformationTask taskWithLazy(boolean lazy) {
        return createTask(builder -> builder
                .name(TRANSFORMATION_NAME)
                .lazy(lazy)
                .uploadId(UPLOAD_ID));
    }
}
