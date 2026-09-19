package com.example.transformer_contracts.notifications;

import com.example.uploads_api.transformations.tasks.TransformationTaskGroup;
import org.jspecify.annotations.NonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ProgressTracker implements AutoCloseable {
    private final TransformationTaskGroup group;
    private final NotificationsApi api;

    private final @NonNull Map<@NonNull String, @NonNull Integer> states;
    private int percent = 0;

    private static final long period = 2;
    private static final long initialDelay = 2;
    private static final TimeUnit unit = TimeUnit.SECONDS;

    private final ScheduledExecutorService scheduler;

    public ProgressTracker(TransformationTaskGroup group, NotificationsApi api) {
        this.group = group;
        this.api = api;

        states = new HashMap<>();
        for (var task : group.tasks())
            states.put(task.name(), 0);

        scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(this::report, initialDelay, period, unit);
    }

    private void report() {
        api.progressWebhook(group, percent);
    }

    private void updatePercent() {
        var avgPercent = states.values().stream().mapToInt(Integer::intValue).average().orElseThrow();
        percent = Math.toIntExact(Math.round(avgPercent));
    }

    public void onProgress(int percent, String name) {
        states.put(name, percent);
        updatePercent();
        System.out.printf("[%s] %s, total: %s\n", name, percent, percent);
    }

    @Override
    public void close() {
        scheduler.shutdown();
    }
}
