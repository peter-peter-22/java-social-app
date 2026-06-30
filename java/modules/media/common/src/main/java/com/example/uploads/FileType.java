package com.example.uploads;

import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;

@AllArgsConstructor
public enum FileType {
    JPEG(new String[]{"jpg","jpeg"}, new String[]{"image/jpeg","image/jpg"}, "application/image", MediaType.IMAGE);

    @NotNull
    public final String[] extensions;
    @NotNull
    public final String[] mimeType;
    @NotNull
    public final String contentType;
    @NotNull
    public final MediaType mediaType;
}
