package com.example.uploads_service_4.upload_service_2;

import org.jspecify.annotations.Nullable;

public class ObjectKeyFactory {
    public static String getOriginalKey(String uploadKey, @Nullable String extension) {
        if (extension == null) return uploadKey + "/original";
        return uploadKey + "/original." + extension;
    }

    public static String getVariantFolder(String uploadKey, String transformation) {
        return uploadKey + "/variants/" + transformation;
    }
}
