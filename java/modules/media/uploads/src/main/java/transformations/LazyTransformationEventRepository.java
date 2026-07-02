package transformations;

import com.example.transformations.UploadTransformation;
import com.example.transformations.UploadTransformationDTO;
import com.example.uploads.UploadId;

import java.util.Collection;

class LazyTransformationEventRepository {
    /** Queue the given transformations for the given upload. */
    public void queueLazyUploadTransformations(UploadId uploadId, Collection<UploadTransformation> transformations) {
        var events = transformations.stream()
                .map(transformation -> UploadTransformationDTO.toDTO(transformation, uploadId))
                .toList();
        System.out.println("Events: " + events);
    }
}
