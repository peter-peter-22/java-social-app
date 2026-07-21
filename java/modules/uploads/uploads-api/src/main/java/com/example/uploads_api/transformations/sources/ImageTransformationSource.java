package com.example.uploads_api.transformations.sources;

import com.example.uploads_api.transformations.operations.ImageTransformationOperations;
import com.example.uploads_api.uploads.MediaType;
import com.example.uploads_api.uploads.Upload;
import lombok.experimental.SuperBuilder;
import org.jspecify.annotations.NonNull;

@SuperBuilder
public class ImageTransformationSource extends TransformationSourceBase<ImageTransformationOperations> {

    @Override
    public boolean isApplicable(@NonNull Upload upload) {
        if (!upload.fileType().getMediaType().equals(MediaType.IMAGE))
            return false;
        return super.isApplicable(upload);
    }

}
