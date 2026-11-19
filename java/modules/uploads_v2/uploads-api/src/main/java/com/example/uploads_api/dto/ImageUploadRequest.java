package com.example.uploads_api.dto;

import com.example.uploads_api.authentication.AuthenticationType;
import lombok.*;
import lombok.extern.jackson.Jacksonized;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.List;

@Builder
@Getter
@EqualsAndHashCode
@ToString
@NullMarked
@AllArgsConstructor
@Jacksonized
public final class ImageUploadRequest {
    private final String uploadKey;
    // TODO for image and video requests, the content type and extension should not be optional
    @Nullable
    private final String extension;
    @Nullable
    private final String desiredContentType;
    @Nullable
    private final Integer desiredBytes;
    @Nullable
    private final List<String> eagerTransformations;
    @Nullable
    private final String incomingTransformation;
    @Nullable
    private final String completionUrl;
    @Nullable
    private final String progressUrl;
    @Builder.Default
    private final boolean async = false;
    @Nullable
    private final AuthenticationType authentication;
}