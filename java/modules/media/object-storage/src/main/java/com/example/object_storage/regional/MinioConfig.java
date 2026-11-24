package com.example.object_storage.regional;

import io.minio.MinioClient;
import org.jspecify.annotations.NullMarked;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Set;

/**
 * One Minio client per configured region. The clients are built eagerly and looked up by the
 * region name a {@code ObjectRef} carries.
 */
@Configuration
@EnableConfigurationProperties(MinioProperties.class)
@NullMarked
public class MinioConfig {
    private final HashMap<String, MinioClient> regions = new HashMap<>();

    MinioConfig(MinioProperties minioProperties) {
        for (var region : minioProperties.regions()) {
            var client = MinioClient.builder()
                    .endpoint(region.endpoint().toString())
                    .credentials(region.accessKey(), region.secretKey())
                    .build();
            regions.put(region.name(), client);
        }
    }

    public MinioClient getClient(String region) {
        return regions.get(region);
    }

    public Set<String> getAllRegions() {
        return regions.keySet();
    }
}