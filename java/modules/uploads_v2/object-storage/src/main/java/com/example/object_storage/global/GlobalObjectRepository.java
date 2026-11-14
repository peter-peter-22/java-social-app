package com.example.object_storage.global;

import com.example.object_storage.regional.RegionalObjectRepositories;
import com.example.object_storage.regional.args.*;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Repository;

import java.io.InputStream;

@Repository
@NullMarked
@RequiredArgsConstructor
public class GlobalObjectRepository {
    private final RegionalObjectRepositories regionalObjectRepositories;

    public void downloadFile(String region, DownloadLocalObjectArgs args) {
        regionalObjectRepositories.getByRegion(region).downloadObject(args);
    }

    public void uploadFile(String region, UploadLocalObjectArgs args) {
        regionalObjectRepositories.getByRegion(region).uploadObject(args);
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

    public void setTags(String region, SetTagsForLocalObjectArgs args) {
        regionalObjectRepositories.getByRegion(region).setTags(args);
    }

    public boolean objectExists(String region, LocalObjectExistsArgs args){
        return regionalObjectRepositories.getByRegion(region).objectExists(args);
    }

    public String getHomeRegion() {
        return regionalObjectRepositories.getHomeRegion();
    }
}
