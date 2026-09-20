package com.example.object_storage.reconciliation;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class MinioReconciliation {
    public abstract String getName();

    /** Apply idempotent settings to minio.*/
    public abstract void apply(MinioClient client);

    protected static void createBucketIfNotExists(MinioClient client, String name) {
        try {
            boolean exists = client.bucketExists(
                    BucketExistsArgs.builder().bucket(name).build()
            );
            if (exists) {
                System.out.printf("Bucket already '%s' already exists.%n", name);
                return;
            }
            System.out.printf("Creating bucket '%s'.", name);
            client.makeBucket(
                    MakeBucketArgs.builder().bucket(name).build()
            );
            System.out.printf("Created bucket '%s' successfully.%n", name);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
