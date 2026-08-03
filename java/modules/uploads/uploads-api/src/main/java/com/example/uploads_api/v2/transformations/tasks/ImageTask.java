package com.example.uploads_api.v2.transformations.tasks;

import com.example.uploads_api.v2.transformations.operations.ImageTransformationOperations;
import com.example.uploads_api.v2.uploads.upload_registry.Upload;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.jspecify.annotations.NonNull;

import java.util.Collection;

@Builder
@Getter
@EqualsAndHashCode
public class ImageTask {
    private final Upload original;
    private final Collection<ImageTransformationInstance> tasks;
    private final String completedNotificationUrl;
    private final String progressNotificationUrl;

    @Builder
    public record ImageTransformationInstance(
            @NonNull String name,
            @NonNull ImageTransformationOperations operations
    ) {
    }
}
