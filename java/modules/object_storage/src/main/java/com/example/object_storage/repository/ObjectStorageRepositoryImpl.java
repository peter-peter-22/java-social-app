package com.example.object_storage.repository;

import com.example.object_storage.MinioProperties;
import io.minio.MinioClient;
import io.minio.PostPolicy;
import io.minio.StatObjectArgs;
import io.minio.UploadObjectArgs;
import io.minio.errors.*;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.ZonedDateTime;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class ObjectStorageRepositoryImpl implements ObjectStorageRepository {
    private final MinioClient minioClient;
    private final MinioProperties minioProperties;

    @Override
    public @NotNull String getDownloadUrl(String bucket, String objectPath) {
        return String.format("%s/%s/%s", minioProperties.endpoint(), bucket, objectPath);
    }

    @Override
    public @NotNull String getPreSignedDownloadUrl() {
        return "not implemented yet";
    }

    @Override
    public @NotNull Map<String, String> getPreSignedUploadUrl(String bucket, String objectPath, Integer expirationSeconds) {
        PostPolicy postPolicy = new PostPolicy(
                "uploads",
                ZonedDateTime.now().plusSeconds(expirationSeconds)
        );
        postPolicy.addEqualsCondition("key", objectPath);
        postPolicy.addContentLengthRangeCondition(1, 10 * 1024 * 1024);
        try {
            return minioClient.getPresignedPostFormData(postPolicy);
        } catch (InvalidResponseException | ErrorResponseException | InsufficientDataException | InternalException |
                 InvalidKeyException | IOException | NoSuchAlgorithmException | ServerException |
                 XmlParserException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteObject(String path) {

    }

    @Override
    public void downloadObject(String path) {

    }

    @Override
    public void uploadObject(String filePath, String objectPath, String bucketName, String contentType) {
        try {
            var args = UploadObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectPath)
                    .filename(filePath)
                    .contentType(contentType)
                    .build();
            minioClient.uploadObject(args);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean objectExists(String bucket, String objectPath) {
        try {
            var args = StatObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectPath)
                    .build();
            var stat = minioClient.statObject(args);
            System.out.println(stat);
            return true;
        } catch (ErrorResponseException e) {
            if ("NoSuchKey".equals(e.errorResponse().code())) {
                return false;
            }
            throw new RuntimeException(e);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
