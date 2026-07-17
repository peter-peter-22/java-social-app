package com.example.uploads_api.uploads;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

@AllArgsConstructor
@Getter
public enum FileType {
    JPEG(new String[]{"jpg", "jpeg"}, new String[]{"image/jpeg", "image/jpg"}, "image/jpg", MediaType.IMAGE),
    WEBP(new String[]{"webp"}, new String[]{"image/webp"}, "image/webp", MediaType.IMAGE),
    MP4(new String[]{"mp4"}, new String[]{"video/mp4"}, "video/mp4", MediaType.VIDEO);

    @NotNull
    private final String[] extensions;
    @NotNull
    private final String[] mimeType;
    @NotNull
    private final String contentType;
    @NotNull
    private final MediaType mediaType;
}
