package com.example.uploads_api.transformations.sources;

import com.example.uploads_api.transformations.dto.ImageTransformationTaskDTO;
import com.example.uploads_api.transformations.operations.ImageTransformationOperations;
import com.example.uploads_api.transformations.path.TransformationPathManager;
import com.example.uploads_api.uploads.MediaType;
import com.example.uploads_api.uploads.Upload;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.jspecify.annotations.NonNull;

@SuperBuilder
@Getter
public class ImageTransformationSource extends TransformationSourceBase implements TransformationSource<ImageTransformationTaskDTO> {
    public @NonNull ImageTransformationOperations operations;

    @Override
    public boolean isApplicable(@NonNull Upload upload) {
        if (!upload.fileType().getMediaType().equals(MediaType.IMAGE))
            return false;
        return super.isApplicable(upload);
    }

    public @NonNull ImageTransformationTaskDTO createTaskDTO(@NonNull Upload original) {
        // TODO separate to mapper class
        // TODO update to new DTO
        return new ImageTransformationTaskDTO(
                original.objectLocation(),
                TransformationPathManager.getOutputObject(original, this),
                getName(),
                isLazy(),
                operations.getLimitWidth(),
                operations.getLimitHeight(),
                operations.getFormat(),
                operations.getQuality(),
                operations.getAspectRatio(),
                original.id()
        );
    }
}
