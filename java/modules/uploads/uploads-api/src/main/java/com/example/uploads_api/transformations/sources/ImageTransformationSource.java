package com.example.uploads_api.transformations.sources;

import com.example.uploads_api.transformations.dto.ImageTransformationTaskDTO;
import com.example.uploads_api.transformations.operations.ImageTransformationOperations;
import com.example.uploads_api.transformations.path.TransformationPathManager;
import com.example.uploads_api.uploads.MediaType;
import com.example.uploads_api.uploads.Upload;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.jetbrains.annotations.NotNull;

@SuperBuilder
@Getter
public class ImageTransformationSource extends TransformationSourceBase implements TransformationSource<ImageTransformationTaskDTO> {
    public @NotNull ImageTransformationOperations operations;

    @Override
    public boolean isApplicable(@NotNull Upload upload) {
        if (!upload.fileType().getMediaType().equals(MediaType.IMAGE))
            return false;
        return super.isApplicable(upload);
    }

    public @NotNull ImageTransformationTaskDTO createTaskDTO(@NotNull Upload original) {
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
