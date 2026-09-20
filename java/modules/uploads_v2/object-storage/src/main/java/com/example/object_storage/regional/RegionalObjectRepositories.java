package com.example.object_storage.regional;

import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
@NullMarked
public class RegionalObjectRepositories {
    private final HashMap<String, ObjectStorageRepository> regions = new HashMap<>();
    private final ObjectStorageRepository homeRepository;

    public RegionalObjectRepositories(MinioConfig minioConfig) {
        for (var region : minioConfig.getAllRegions()) {
            var repository = new ObjectStorageRepositoryImpl(minioConfig.getClient(region));
            regions.put(region, repository);
        }
        homeRepository = regions.get(minioConfig.getHomeRegion());
    }

    public ObjectStorageRepository getByRegion(String region) {
        return regions.get(region);
    }

    public ObjectStorageRepository getLocal() {
        return homeRepository;
    }
}
