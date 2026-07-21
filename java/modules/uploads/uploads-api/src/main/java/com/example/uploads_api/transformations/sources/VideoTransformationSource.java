package com.example.uploads_api.transformations.sources;

import com.example.uploads_api.transformations.dto.VideoTransformationTaskDTO;
import com.example.uploads_api.transformations.operations.VideoTransformationOperations;
import com.example.uploads_api.transformations.path.TransformationPathManager;
import com.example.uploads_api.uploads.MediaType;
import com.example.uploads_api.uploads.Upload;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.jspecify.annotations.NonNull;

@SuperBuilder
@Getter
public class VideoTransformationSource extends TransformationSourceBase<VideoTransformationTaskDTO> {
    public @NonNull VideoTransformationOperations operations;

    @Override
    public boolean isApplicable(@NonNull Upload upload) {
        if (!upload.fileType().getMediaType().equals(MediaType.VIDEO))
            return false;
        return super.isApplicable(upload);
    }

    public @NonNull VideoTransformationTaskDTO createTaskDTO(@NonNull Upload original) {
        return new VideoTransformationTaskDTO(
                original.objectLocation(),
                TransformationPathManager.getOutputObject(original,this),
                getName(),
                isLazy(),
                getOutputBucket(),
                operations.getLimitWidth(),
                operations.getLimitHeight(),
                operations.getFormat(),
                operations.getQuality(),
                operations.getAspectRatio()
        );
    }
}
