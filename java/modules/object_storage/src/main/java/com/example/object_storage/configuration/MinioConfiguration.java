package com.example.object_storage.configuration;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class MinioConfiguration {
    protected final MinioClient minioClient;
    public abstract String getName();

    /** Apply idempotent settings to minio.*/
    public abstract void apply() throws Exception;

    protected void createBucketIfNotExists(String name)  {
        try {
            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(name).build()
            );
            if (exists) {
                System.out.printf("Bucket already '%s' already exists.%n", name);
                return;
            }
            System.out.printf("Creating bucket '%s'.", name);
            minioClient.makeBucket(
                    MakeBucketArgs.builder().bucket(name).build()
            );
            System.out.printf("Created bucket '%s' successfully.%n", name);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
