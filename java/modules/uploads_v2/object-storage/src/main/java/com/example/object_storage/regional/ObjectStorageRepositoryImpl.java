package com.example.object_storage.regional;

import com.example.object_storage.regional.args.*;
import com.example.object_storage.tags.ObjectTag;
import io.minio.*;
import io.minio.errors.ErrorResponseException;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;

import java.io.InputStream;

@RequiredArgsConstructor
@NullMarked
class ObjectStorageRepositoryImpl implements ObjectStorageRepository {
    private final MinioClient minioClient;

    @Override
    public void deleteObject(DeleteLocalObjectArgs args) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(args.getBucket())
                    .object(args.getKey())
                    .build());
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete object from MinIO", e);
        }
    }

    @Override
    public InputStream getObject(GetLocalObjectArgs args) {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(args.getBucket())
                            .object(args.getKey())
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to read object from MinIO", e);
        }
    }

    @Override
    public boolean objectExists(LocalObjectExistsArgs args) {
        try {
            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(args.getBucket())
                            .object(args.getKey())
                            .build()
            );
            return true;
        } catch (ErrorResponseException e) {
            if ("NoSuchKey".equals(e.errorResponse().code())) {
                return false;
            }
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void putObject(PutLocalObjectArgs args) {
        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(args.getBucket())
                            .object(args.getKey())
                            .stream(args.getInputStream(), args.getContentLength(), -1)
                            .contentType(args.getContentType())
                            .tags(args.getTags()==null?null:ObjectTag.toMap(args.getTags()))
                            .build());
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload input stream", e);
        }
    }

    @Override
    public void downloadObject(DownloadLocalObjectArgs args) {
        try {
            minioClient.downloadObject(
                    DownloadObjectArgs.builder()
                            .bucket(args.getBucket())
                            .object(args.getKey())
                            .filename(args.getDestinationPath())
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to download file", e);
        }
    }

    @Override
    public void uploadObject(UploadLocalObjectArgs args) {
        try {
            minioClient.uploadObject(
                    UploadObjectArgs.builder()
                            .bucket(args.getBucket())
                            .object(args.getKey())
                            .filename(args.getSourcePath())
                            .contentType(args.getContentType())
                            .tags(args.getTags()==null?null:ObjectTag.toMap(args.getTags()))
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload file", e);
        }
    }

    @Override
    public void setTags(SetTagsForLocalObjectArgs args){
        // TODO test if this function keep the previous tags or not
        // TODO test what happens with null tags
        try {
            minioClient.setObjectTags(
                    SetObjectTagsArgs.builder()
                            .bucket(args.getBucket())
                            .object(args.getKey())
                            .tags(args.getTags() == null ? null : ObjectTag.toMap(args.getTags()))
                            .build()
            );
        }
        catch (Exception e) {
            throw new RuntimeException("Failed to set tags for object", e);
        }
    }
}
