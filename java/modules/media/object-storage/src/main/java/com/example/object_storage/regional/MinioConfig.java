package com.example.object_storage.regional;

import io.minio.MinioClient;
import lombok.Getter;
import org.jspecify.annotations.NullMarked;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Set;

@Configuration
@EnableConfigurationProperties(MinioProperties.class)
@NullMarked
public class MinioConfig {
    private final HashMap<String, MinioClient> regions = new HashMap<>();
    @Getter
    private final String homeRegion;

    MinioConfig(MinioProperties minioProperties) {
        for (var region : minioProperties.regions()) {
            var client = MinioClient.builder()
                    .endpoint(region.endpoint().toString())
                    .credentials(region.accessKey(), region.secretKey())
                    .build();
            regions.put(region.name(), client);
        }
        this.homeRegion = minioProperties.homeRegion();
    }

    public MinioClient getClient(String region) {
        return regions.get(region);
    }

    public Set<String> getAllRegions() {
        return regions.keySet();
    }
}