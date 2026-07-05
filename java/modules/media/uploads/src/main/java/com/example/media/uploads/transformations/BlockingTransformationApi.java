package com.example.media.uploads.transformations;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

@Component("blocking")
@RequiredArgsConstructor
class BlockingTransformationApi extends TransformationApiSelector{
    private final ImageTransformerRestApi imageRestApi;
    private final VideoTransformerRestApi videoRestApi;

    @Override
    @NonNull
    TransformationApi getVideoApi() {
        return imageRestApi;
    }

    @Override
    @NonNull
    TransformationApi getImageApi() {
        return videoRestApi;
    }
}
