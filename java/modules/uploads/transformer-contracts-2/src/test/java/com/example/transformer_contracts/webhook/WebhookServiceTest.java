package com.example.transformer_contracts.webhook;

import com.example.uploads_api.transformations.tasks.ImageTransformationTaskGroup;
import com.example.uploads_api.transformations.tasks.TransformationTaskGroup;
import com.example.uploads_api.transformations.webhook.WebhookCall;
import com.example.uploads_api.utils.TestTransformationTaskGroupCreator;
import com.example.uploads_api.v2.transformations.operations.ImageTransformationOperations;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class WebhookServiceTest {
    private static final String TRANSFORMATION_NAME = "thumbnail";

    @Mock
    private WebhookApi webhookApi;

    @Test
    void callsWebhookForLazyTask() {
        var service = new WebhookService(webhookApi);
        var task = taskWithLazy(true);
        var uploadId = task.uploadId();

        service.handleWebhookCall(task);

        var callCaptor = ArgumentCaptor.forClass(WebhookCall.class);
        verify(webhookApi).call(callCaptor.capture());
        assertThat(callCaptor.getValue())
                .isEqualTo(new WebhookCall(uploadId, TRANSFORMATION_NAME));
    }

    @Test
    void doesNotCallWebhookForNonLazyTask() {
        var service = new WebhookService(webhookApi);

        service.handleWebhookCall(taskWithLazy(false));

        verifyNoInteractions(webhookApi);
    }


    private static TransformationTaskGroup taskWithLazy(boolean lazy) {
        return TestTransformationTaskGroupCreator.createImageTransformationTaskGroup(
                c -> c.tasks(
                        List.of(
                                ImageTransformationTaskGroup.ImageTask.builder()
                                        .name(TRANSFORMATION_NAME)
                                        .lazy(lazy)
                                        .operations(ImageTransformationOperations.builderWithDefaults().build())
                                        .build()
                        )
                )
        );
    }
}
