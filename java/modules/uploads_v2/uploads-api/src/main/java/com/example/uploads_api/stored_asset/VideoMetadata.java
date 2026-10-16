package com.example.uploads_api.stored_asset;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.jspecify.annotations.NullMarked;

@NullMarked
@Getter
@Builder
@EqualsAndHashCode
public final class VideoMetadata {
    private final Integer resolutionX;
    private final Integer resolutionY;
    private final Integer durationMilliseconds;
}
