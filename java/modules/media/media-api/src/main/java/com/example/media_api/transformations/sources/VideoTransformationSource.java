package com.example.media_api.transformations.sources;

import com.example.media_api.transformations.dto.VideoTransformationTaskDTO;
import com.example.media_api.transformations.operations.VideoTransformationOperations;
import com.example.media_api.transformations.path.TransformationPathManager;
import com.example.media_api.uploads.MediaType;
import com.example.media_api.uploads.Upload;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.jetbrains.annotations.NotNull;

@SuperBuilder
@Getter
public class VideoTransformationSource extends TransformationSourceBase implements TransformationSource<VideoTransformationTaskDTO> {
    public @NotNull VideoTransformationOperations operations;

    @Override
    public boolean isApplicable(@NotNull Upload upload) {
        if (!upload.fileType().getMediaType().equals(MediaType.VIDEO))
            return false;
        return super.isApplicable(upload);
    }

    public @NotNull VideoTransformationTaskDTO createTaskDTO(@NotNull Upload original) {
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
