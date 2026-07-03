package com.example.media.common.uploads;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

@AllArgsConstructor
@Getter
public enum FileType {
    JPEG(new String[]{"jpg","jpeg"}, new String[]{"image/jpeg","image/jpg"}, "application/image", MediaType.IMAGE);

    @NotNull
    private final String[] extensions;
    @NotNull
    private final String[] mimeType;
    @NotNull
    private final String contentType;
    @NotNull
    private final MediaType mediaType;
}
