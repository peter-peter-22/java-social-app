package com.example.media.api;

import com.example.media.contract.port.AssetStore;
import com.example.media.contract.port.ImageTransformer;
import com.example.media.contract.port.ObjectStore;
import com.example.media.contract.port.TransformationCatalog;
import com.example.media.contract.port.UploadRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * The context smoke test. The driven adapters arrive with the wiring step; until then the ports
 * stand in as mocks so the application context still loads.
 */
@SpringBootTest
class ApplicationTest {

    @MockitoBean
    UploadRegistry uploadRegistry;

    @MockitoBean
    AssetStore assetStore;

    @MockitoBean
    TransformationCatalog transformationCatalog;

    @MockitoBean
    ObjectStore objectStore;

    @MockitoBean
    ImageTransformer imageTransformer;

    @MockitoBean
    TransactionTemplate transactionTemplate;

    @Test
    void contextLoads() {
    }
}
