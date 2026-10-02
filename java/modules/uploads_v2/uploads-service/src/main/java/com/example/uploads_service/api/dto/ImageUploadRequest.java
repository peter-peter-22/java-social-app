package com.example.uploads_service.api.dto;

import com.example.image_transformer.api.authentication.AuthenticationType;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.List;

@Builder
@Getter
@EqualsAndHashCode
@NullMarked
public final class ImageUploadRequest {
    private final String uploadKey;
    @Nullable
    private final String contentType;
    @Nullable
    private final Integer bytes;
    @Nullable
    private final List<String> eagerTransformations;
    @Nullable
    private final List<String> incomingTransformations;
    @Nullable
    private final String completionUrl;
    @Nullable
    private final String progressUrl;
    @Builder.Default
    private final boolean asyncEager = false;
    @Nullable
    private final AuthenticationType authentication;
}