package com.example.image_transformer.api.processing;

import com.example.image_transformer.api.authentication.AuthenticationType;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@Builder
@NullMarked
@EqualsAndHashCode
@Getter
public final class ImageUploadTask {
    String uploadKey;
    @Nullable String uploadPreset;
}
