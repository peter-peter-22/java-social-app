package com.example.uploads_api.dto;

import com.example.uploads_api.authentication.AuthenticationType;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.List;

@Builder
@Getter
@EqualsAndHashCode
@ToString
@NullMarked
public final class ImageUploadRequest {
    private final String uploadKey;
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