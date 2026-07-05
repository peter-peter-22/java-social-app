package com.example.media_api.uploads;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

@AllArgsConstructor
@Getter
public enum FileType {
    JPEG(new String[]{"jpg", "jpeg"}, new String[]{"image/jpeg", "image/jpg"}, "application/image", MediaType.IMAGE),
    MP4(new String[]{"mp4"}, new String[]{"video/mp4"}, "application/image", MediaType.VIDEO);

    @NotNull
    private final String[] extensions;
    @NotNull
    private final String[] mimeType;
    @NotNull
    private final String contentType;
    @NotNull
    private final MediaType mediaType;
}
