package com.example.transformer_contracts.notifications;

import com.example.uploads_api.transformations.tasks.TransformationTaskGroup;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProgressTrackerFactory {
    private final NotificationsApi api;

    public ProgressTracker createProgressTracker(TransformationTaskGroup taskGroup) {
        return new ProgressTracker(taskGroup, api);
    }
}
