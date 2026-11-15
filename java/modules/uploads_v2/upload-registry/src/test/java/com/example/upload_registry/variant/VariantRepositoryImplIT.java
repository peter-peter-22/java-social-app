package com.example.upload_registry.variant;

import com.example.cockroach_db.CockroachIntegrationTest;
import com.example.upload_registry.transformations.NamedTransformationCrudRepository;
import com.example.upload_registry.transformations.NamedTransformationRepositoryImpl;
import com.example.upload_registry.upload.UploadRepositoryImpl;
import com.example.uploads_api.stored_asset.AssetDeliveryProtocol;
import com.example.uploads_api.stored_asset.InsertStoredAsset;
import com.example.uploads_api.stored_asset.StoredAssetRepository;
import com.example.uploads_api.stored_asset.StoredAssetStatus;
import com.example.uploads_api.transformations.ImageEncodings;
import com.example.uploads_api.transformations.ImageTransformationOperations;
import com.example.uploads_api.transformations.named_transformation_repository.InsertNamedImageTransformation;
import com.example.uploads_api.uploads.InsertUpload;
import com.example.uploads_api.variant.InsertVariant;
import com.example.uploads_api.variant.VariantId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class VariantRepositoryImplIT extends CockroachIntegrationTest {
    @Autowired
    private VariantRepositoryImpl variantRepository;
    @Autowired
    private StoredAssetRepository storedAssetRepository;
    @Autowired
    private UploadRepositoryImpl uploadRepository;
    @Autowired
    private NamedTransformationRepositoryImpl namedTransformationRepository;
    @Autowired
    private NamedTransformationCrudRepository namedTransformationCrudRepository;

    @Test
    void insertsFindsAndDeletesVariant() {
        var uploadKey = "variant-" + UUID.randomUUID();
        var uploadId = uploadRepository.insert(InsertUpload.builder()
                .uploadKey(uploadKey)
                .storedAssetId(insertAsset().getId())
                .build());
        var transformation = namedTransformationRepository.get(insertTransformation());
        var variantAsset = insertAsset();

        var inserted = variantRepository.insert(InsertVariant.builder()
                .id(VariantId.builder()
                        .uploadId(uploadId)
                        .namedTransformationId(namedTransformationId(transformation.getName()))
                        .build())
                .transformationVersionId(transformation.getCurrentVersionId())
                .storedAssetId(variantAsset.getId())
                .build());

        var found = variantRepository.findByUploadKeyAndTransformationName(uploadKey, transformation.getName());
        assertThat(found.getId()).isEqualTo(inserted);
        assertThat(found.getStoredAssetId()).isEqualTo(variantAsset.getId());
        assertThat(found.getTransformationVersionId()).isEqualTo(transformation.getCurrentVersionId());

        variantRepository.delete(inserted);
        assertThat(storedAssetRepository.findById(variantAsset.getId()).getStatus()).isEqualTo(StoredAssetStatus.DELETE);
    }

    private com.example.uploads_api.stored_asset.StoredAsset insertAsset() {
        return storedAssetRepository.insert(InsertStoredAsset.builder()
                .homeRegion("eu-central-1")
                .objectBucket("uploads")
                .directory(false)
                .entryPoint("file.jpg")
                .contentType("image/jpeg")
                .extension("jpg")
                .bytes(42)
                .objects(List.of("file.jpg"))
                .deliveryProtocol(AssetDeliveryProtocol.IMAGE)
                .build());
    }

    private UUID namedTransformationId(String name) {
        return namedTransformationCrudRepository.findByName(name).orElseThrow().getId();
    }

    private String insertTransformation() {
        var name = "variant-" + UUID.randomUUID().toString().substring(0, 12);
        namedTransformationRepository.set(new InsertNamedImageTransformation(name, ImageTransformationOperations.builder()
                .encoding(ImageEncodings.Jpeg.builder().quality(80).build())
                .build()));
        return name;
    }
}
