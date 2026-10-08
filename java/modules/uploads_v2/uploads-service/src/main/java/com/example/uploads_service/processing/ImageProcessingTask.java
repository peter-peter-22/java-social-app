package com.example.uploads_service.processing;

import com.example.uploads_service.api.transformations.NamedImageTransformation;
import com.example.uploads_service.api.uploads.Upload;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Collection;

@Builder
@NullMarked
@EqualsAndHashCode
@Getter
public final class ImageProcessingTask {
    private final Upload upload;
    @Nullable
    private final Collection<NamedImageTransformation> eager;
    @Nullable
    private final Collection<NamedImageTransformation> incoming;
    @Nullable
    private final String completionUrl;
    @Nullable
    private final String progressUrl;
}
