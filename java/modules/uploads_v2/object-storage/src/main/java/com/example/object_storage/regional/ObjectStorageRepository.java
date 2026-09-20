package com.example.object_storage.regional;

import com.example.object_storage.regional.args.*;
import org.jspecify.annotations.NullMarked;

import java.io.InputStream;

@NullMarked
public interface ObjectStorageRepository {
    void deleteObject(DeleteLocalObjectArgs args);

    InputStream getObject(GetLocalObjectArgs args);

    boolean objectExists(LocalObjectExistsArgs args);

    void putObject(PutLocalObjectArgs args);

    void downloadObject(DownloadLocalObjectArgs args);

    void uploadObject(UploadLocalObjectArgs args);
}