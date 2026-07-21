package com.example.uploads_api.transformations.path;

import com.example.uploads_api.transformations.sources.TransformationSource;
import com.example.uploads_api.uploads.ObjectLocation;
import com.example.uploads_api.uploads.Upload;
import org.jspecify.annotations.NonNull;

public class TransformationPathManager {
    public static @NonNull ObjectLocation getOutputObject(@NonNull Upload original, @NonNull String extension, @NonNull String outputBucket, @NonNull String name) {
        return new ObjectLocation(original.objectLocation().bucket() + "/" + original.objectLocation().key() + "/" + name + "." + extension, outputBucket);
    }

    public static @NonNull ObjectLocation getOutputObject(@NonNull Upload original, @NonNull TransformationSource transformation) {
        var extension = transformation.getOperations().getFormat().getExtensions()[0];
        return getOutputObject(original, extension, transformation.getOutputBucket(), transformation.getName());
    }
}
