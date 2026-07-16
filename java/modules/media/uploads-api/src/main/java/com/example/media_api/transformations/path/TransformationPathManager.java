package com.example.media_api.transformations.path;

import com.example.media_api.transformations.sources.TransformationSource;
import com.example.media_api.uploads.ObjectLocation;
import com.example.media_api.uploads.Upload;
import org.jetbrains.annotations.NotNull;

public class TransformationPathManager {
    public static @NotNull ObjectLocation getOutputObject(@NotNull Upload original, @NotNull String extension, @NotNull String outputBucket, @NotNull String name) {
        return new ObjectLocation(original.objectLocation().bucket() + "/" + original.objectLocation().path() + "/" + name + "." + extension, outputBucket);
    }

    public static @NotNull ObjectLocation getOutputObject(@NotNull Upload original, @NotNull TransformationSource<?> transformation){
        var extension = transformation.getOperations().getFormat().getExtensions()[0];
        return getOutputObject(original, extension, transformation.getOutputBucket(), transformation.getName());
    }
}
