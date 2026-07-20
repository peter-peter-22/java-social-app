package com.example.uploads_api.uploads;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jspecify.annotations.NonNull;

@AllArgsConstructor
@Getter
public enum FileType {
    JPEG(new String[]{"jpg", "jpeg"}, new String[]{"image/jpeg", "image/jpg"}, "image/jpg", MediaType.IMAGE),
    WEBP(new String[]{"webp"}, new String[]{"image/webp"}, "image/webp", MediaType.IMAGE),
    MP4(new String[]{"mp4"}, new String[]{"video/mp4"}, "video/mp4", MediaType.VIDEO);

    @NonNull
    private final String[] extensions;
    @NonNull
    private final String[] mimeType;
    @NonNull
    private final String contentType;
    @NonNull
    private final MediaType mediaType;
}
