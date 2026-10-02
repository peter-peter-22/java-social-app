package com.example.uploads_service.api.transformations;

import com.example.image_transformer.api.authentication.AuthenticationType;
import lombok.Builder;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@Builder
@NullMarked
public final class ImageUploadPreset {
    String name;
    NamedImageTransformation transformation;
    @Nullable AuthenticationType authentication;
}
