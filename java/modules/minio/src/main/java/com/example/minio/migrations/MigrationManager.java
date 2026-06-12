package com.example.minio.migrations;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.errors.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class MigrationManager {
    private MinioClient minioClient;

    public MigrationManager(@Autowired MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    /** Apply idempotent settings to minio.
     * Deletions are not supported. */
    public void applyMigrations() {
        try {
            createBucketIfNotExists("test");
        }
        catch (Exception e) {
            System.out.println("Failed to apply migrations.");
            throw new RuntimeException(e);
        }
    }

    private void createBucketIfNotExists(String name) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        boolean exists = minioClient.bucketExists(
                BucketExistsArgs.builder().bucket(name).build()
        );
        if (exists){
            System.out.printf("Bucket already '%s' already exists.", name);
            return;
        }
        System.out.printf("Creating bucket '%s'.", name);
        minioClient.makeBucket(
                MakeBucketArgs.builder().bucket(name).build()
        );
        System.out.printf("Created bucket '%s' successfully.", name);
    }
}
