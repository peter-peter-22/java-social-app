package com.example.object_storage.global;

import com.example.object_storage.regional.RegionalObjectRepositories;
import com.example.object_storage.regional.args.*;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Repository;

import java.io.InputStream;
import java.util.Collection;
import java.util.concurrent.Executors;

@Repository
@NullMarked
@RequiredArgsConstructor
public class GlobalObjectRepository {
    private final RegionalObjectRepositories regionalObjectRepositories;

    public void download(String region, DownloadLocalObjectArgs args) {
        regionalObjectRepositories.getByRegion(region).downloadObject(args);
    }

    public void upload(String region, UploadLocalObjectArgs args) {
        regionalObjectRepositories.getByRegion(region).uploadObject(args);
    }

    public void uploadAll(String region, Collection<UploadLocalObjectArgs> args) {
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (UploadLocalObjectArgs arg : args) {
                executor.submit(() -> upload(region, arg));
            }
        }
    }

    public void delete(String region, DeleteLocalObjectArgs args) {
        regionalObjectRepositories.getByRegion(region).deleteObject(args);
    }

    public InputStream getObject(String region, GetLocalObjectArgs args) {
        return regionalObjectRepositories.getByRegion(region).getObject(args);
    }

    public void putObject(String region, PutLocalObjectArgs args) {
        regionalObjectRepositories.getByRegion(region).putObject(args);
    }
}
