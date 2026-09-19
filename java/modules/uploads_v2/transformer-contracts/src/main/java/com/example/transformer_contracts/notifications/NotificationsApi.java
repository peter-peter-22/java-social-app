package com.example.transformer_contracts.notifications;

import com.example.uploads_api.transformations.tasks.TransformationTaskGroup;
import org.springframework.stereotype.Component;

@Component
public class NotificationsApi {
    public void completionWebhook(TransformationTaskGroup taskGroup) {

    }

    public void progressWebhook(TransformationTaskGroup taskGroup, int percent) {
    }
}
