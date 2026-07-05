package com.example.media.uploads.transformations;

import com.example.media.common.transformations.api.UploadTransformationDTO;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
class LazyTransformationApi implements TransformationApi {
    /**
     * Queue the given transformations for the given upload with a kafka event.
     */
    @Override
    public void transform(@NonNull Collection<UploadTransformationDTO> transformations) {
        System.out.println("Events: " + transformations);
    }
}
