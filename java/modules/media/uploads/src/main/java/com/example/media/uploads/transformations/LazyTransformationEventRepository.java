package com.example.media.uploads.transformations;

import com.example.media.common.transformations.UploadTransformation;
import com.example.media.common.transformations.UploadTransformationDTO;
import com.example.media.common.uploads.UploadId;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
class LazyTransformationEventRepository {
    /** Queue the given transformations for the given upload. */
    public void queueLazyUploadTransformations(UploadId uploadId, Collection<UploadTransformation> transformations) {
        var events = transformations.stream()
                .map(transformation -> UploadTransformationDTO.toDTO(transformation, uploadId))
                .toList();
        System.out.println("Events: " + events);
    }
}
