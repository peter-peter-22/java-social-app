package com.example.upload_registry.stored_asset;

import com.example.cockroach_db.CockroachIntegrationTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class StoredAssetRepositoryImplIT extends CockroachIntegrationTest {
    @Autowired
    private StoredAssetRepositoryImpl storedAssetRepository;
    @Autowired
    private ImageMetadataCrudRepository imageMetadataCrudRepository;
    @Autowired
    private VideoMetadataCrudRepository videoMetadataCrudRepository;
}
