package com.example.transformer_contracts;

import com.example.transformer_contracts.notifications.NotificationsApi;
import com.example.transformer_contracts.notifications.ProgressTracker;
import com.example.transformer_contracts.notifications.ProgressTrackerFactory;
import com.example.transformer_contracts.persistence.TransformationPersistenceSessionFactory;
import com.example.uploads_api.transformations.asset.Asset;
import com.example.uploads_api.transformations.tasks.TransformationTaskGroup;
import com.example.uploads_api.uploads.FileType;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.function.Consumer;

@RequiredArgsConstructor
public class TransformationManager {
    private final TransformationPersistenceSessionFactory persistenceSessionFactory;
    private final FileTransformer fileTransformer;
    private final NotificationsApi notificationsApi;
    private final ProgressTrackerFactory progressTrackerFactory;

    // autowired: objectRepo, transformer
    // assumed: progressApi, completedApi
    void processGroup(TransformationTaskGroup group) {
        var t = Instant.now().getEpochSecond(); // 1785756693

        transformationsWithTracking(group);
    }

    private void transformations(TransformationTaskGroup group, ProgressTracker progressTracker) {
        var a = new Asset("1", FileType.JPEG);
        try (var session = persistenceSessionFactory.createSession(a)) {
            for (var task : group.tasks()) {
                var taskSession = session.createTaskSession(task.name());
                Consumer<Integer> onProgress = progressTracker == null ? null : percent -> progressTracker.onProgress(percent, task.name());
                fileTransformer.transform(
                        FileTransformationArgs.builder()
                                .input(session.getInputFile())
                                .task(task)
                                .outputDir(taskSession.getTaskDir())
                                .onProgress(onProgress)
                                .build()
                );
            }
            if (group.completedNotificationUrl() != null)
                notificationsApi.completionWebhook(group);
        }
    }

    private void transformationsWithTracking(TransformationTaskGroup group) {
        if (group.progressNotificationUrl() == null)
            transformations(group, null);

        try (var tracker = progressTrackerFactory.createProgressTracker(group)) {
            transformations(group, tracker);
        }
    }
}
