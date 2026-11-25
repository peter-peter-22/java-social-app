package com.example.object_storage.reconciliation;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;

@NullMarked
@Slf4j
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
                log.info("Bucket '{}' already exists.", name);
                return;
            }
            log.info("Creating bucket '{}'.", name);
            client.makeBucket(
                    MakeBucketArgs.builder().bucket(name).build()
            );
            log.info("Created bucket '{}' successfully.", name);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
