package com.example.uploads_service.api.transformations.operations;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public interface HasVideoQuality {
    @Nullable Integer getConstantRateFactor();
    @Nullable VideoQualityPreset getVideoQualityPreset();
}
