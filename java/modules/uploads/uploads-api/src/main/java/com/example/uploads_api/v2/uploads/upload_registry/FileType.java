package com.example.uploads_api.v2.uploads.upload_registry;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.nio.file.Path;

@AllArgsConstructor
@Getter
public enum FileType {
    JPEG(new String[]{"jpg", "jpeg"}, new String[]{"image/jpeg", "image/jpg"}, "image/jpg", MediaType.IMAGE),
    WEBP(new String[]{"webp"}, new String[]{"image/webp"}, "image/webp", MediaType.IMAGE),
    MP4(new String[]{"mp4"}, new String[]{"video/mp4"}, "video/mp4", MediaType.VIDEO),
    HLS(new String[]{"m3u8"}, new String[]{"application/vnd.apple.mpegurl"}, "application/vnd.apple.mpegurl", MediaType.VIDEO),
    HLS_SEGMENT(new String[]{"ts"}, new String[]{"video/MP2T"}, "video/MP2T", MediaType.VIDEO),
    ANY(null, null, "application/octet-stream", MediaType.RAW);

    @NonNull
    private final String[] extensions;
    @NonNull
    private final String[] mimeType;
    @NonNull
    private final String contentType;
    @NonNull
    private final MediaType mediaType;

    @NonNull
    public String getExtension() {
        return extensions[0];
    }

    private static @Nullable String getFileExtension(Path path) {
        var fileName = path.getFileName().toString();
        var dot = fileName.lastIndexOf(".");
        if (dot == -1) return null;
        return fileName.substring(dot + 1);
    }

    public static @Nullable FileType fromPath(@NonNull Path path) {
        var ext = getFileExtension(path);
        if (ext == null) return null;
        return getByExtension(ext);
    }

    public static @Nullable FileType getByExtension(String extension) {
        for (FileType fileType : FileType.values()) {
            if (extension.equals(fileType.getExtension())) return fileType;
        }
        return null;
    }

    public static @Nullable FileType getByContentType(String contentType) {
        for (FileType fileType : FileType.values()) {
            if (contentType.equals(fileType.getContentType())) return fileType;
        }
        return null;
    }
}
