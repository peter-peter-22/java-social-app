package transformations;

import com.example.transformations.UploadTransformation;
import com.example.uploads.Upload;
import com.example.uploads.UploadId;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

@Service
@AllArgsConstructor
class TransformationService {
    private final LazyTransformationSessionRepository lazyTransformationSessionRepository;
    private final LazyTransformationEventRepository lazyTransformationEventRepository;
    private final BlockingTransformationRepository blockingTransformationRepository;
    private final List<UploadTransformation> transformations;

    /**
     * Completes or queues all matching transformations for the upload.
     *
     * @return True if there is at least one lazy transformation in progress.
     */
    public boolean applyTransformations(@NotNull Upload upload) {
        // get which transformations are applicable based on their filters and the upload
        var applicableTransformations = transformations.stream()
                .filter(transformation -> transformation.isApplicable(upload))
                .toList();

        // wait for blocking transformations to complete
        applyBlockingTransformations(applicableTransformations, upload.id());

        // queue lazy transformations if any, return true if there is at least one
        return applyLazyTransformations(applicableTransformations, upload.id());
    }

    private void applyBlockingTransformations(@NotNull Collection<UploadTransformation> applicableTransformations, UploadId uploadId) {
        var blockingTransformations = applicableTransformations.stream()
                .filter(UploadTransformation::isLazy)
                .toList();

        blockingTransformationRepository.applyTransformations(uploadId, blockingTransformations);
    }

    /**
     * Creates lazy transformation session and events if necessary.
     *
     * @return True if there is at least one lazy transformation.
     */
    private boolean applyLazyTransformations(@NotNull Collection<UploadTransformation> applicableTransformations, UploadId uploadId) {
        // get the list of the lazy transformations
        var lazyTransformations = applicableTransformations.stream()
                .filter(UploadTransformation::isLazy)
                .map(UploadTransformation::getName)
                .toList();

        // if no lazy transformations found, exit
        if (lazyTransformations.isEmpty()) return false;

        var names = applicableTransformations.stream()
                .filter(UploadTransformation::isLazy)
                .map(UploadTransformation::getName)
                .toList();

        // if lazy transformations are required, create the lazy transformation session and events
        lazyTransformationSessionRepository.createLazyTransformationSession(uploadId, names);
        lazyTransformationEventRepository.queueLazyUploadTransformations(uploadId, applicableTransformations);

        return true;
    }
}
