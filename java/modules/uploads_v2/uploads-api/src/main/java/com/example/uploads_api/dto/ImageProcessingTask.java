package com.example.uploads_api.dto;

import com.example.uploads_api.stored_asset.StoredAsset;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.UUID;

@Builder
@NullMarked
@Value
@Jacksonized
public class ImageProcessingTask {
    StoredAsset originalAsset;
    String uploadKey;
    UUID attemptId;
    @Nullable Collection<String> eagerTransformationNames;
    @Nullable String incomingTransformationName;
}
