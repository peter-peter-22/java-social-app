package com.example.uploads_api.utils;

import com.example.uploads_api.transformations.tasks.ImageTransformationMapper;
import com.example.uploads_api.transformations.tasks.ImageTransformationTaskGroup;
import com.example.uploads_api.transformations.tasks.VideoTransformationMapper;
import com.example.uploads_api.transformations.tasks.VideoTransformationTaskGroup;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

public class TestTransformationTaskGroupCreator {

    public static @NonNull ImageTransformationTaskGroup createImageTransformationTaskGroup(
            @Nullable Consumer<ImageTransformationTaskGroup.@NonNull ImageTransformationTaskGroupBuilder> customizer
    ) {
        var upload = TestUploadCreator.createImage();
        var source = TestTransformationSourceCreator.createImageTransformation();
        var exampleTaskGroup = ImageTransformationMapper.createTaskGroupDTO(upload, List.of(source));
        var builder = ImageTransformationTaskGroup.builder()
                .inputObject(exampleTaskGroup.inputObject())
                .uploadId(exampleTaskGroup.uploadId())
                .tasks(exampleTaskGroup.tasks());
        if (customizer != null)
            customizer.accept(builder);
        return builder.build();
    }

    public static @NonNull VideoTransformationTaskGroup createVideoTransformationTaskGroup(
            @Nullable Consumer<VideoTransformationTaskGroup.@NonNull VideoTransformationTaskGroupBuilder> customizer
    ) {
        var upload = TestUploadCreator.createImage();
        var source = TestTransformationSourceCreator.createVideoTransformation();
        var exampleTaskGroup = VideoTransformationMapper.createTaskGroupDTO(upload, List.of(source));
        var builder = VideoTransformationTaskGroup.builder()
                .inputObject(exampleTaskGroup.inputObject())
                .uploadId(exampleTaskGroup.uploadId())
                .tasks(exampleTaskGroup.tasks());
        if (customizer != null)
            customizer.accept(builder);
        return builder.build();
    }
}
