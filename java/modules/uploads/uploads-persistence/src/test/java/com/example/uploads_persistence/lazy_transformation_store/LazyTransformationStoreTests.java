package com.example.uploads_persistence.lazy_transformation_store;

import com.example.cockroach_db.CockroachIntegrationTest;
import com.example.uploads_api.transformations.lazy_transformation_store.LazyTransformationStore;
import com.example.uploads_api.uploads.UploadStatus;
import com.example.uploads_persistence.upload_repository.UploadRepository;
import com.example.uploads_persistence.utils.TestUploadPersistence;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class LazyTransformationStoreTests extends CockroachIntegrationTest {
    @Autowired
    private LazyTransformationStore lazyTransformationStore;
    @Autowired
    private TestUploadPersistence testUploadPersistence;
    @Autowired
    private UploadRepository uploadRepository;

    @Test
    void testCreation() {
        var id = testUploadPersistence.insertUpload();
        var transformations = List.of("1", "2", "3");
        lazyTransformationStore.createLazyTransformationSession(id, transformations);
    }

    @Test
    void testCompletion() {
        var id = testUploadPersistence.insertUpload(c->c.status(UploadStatus.PROCESSING));
        var transformations = List.of("1", "2", "3");
        lazyTransformationStore.createLazyTransformationSession(id, transformations);

        for (var transformation : transformations) {
            lazyTransformationStore.markLazyTransformationAsComplete(id, transformation);

            var upload = uploadRepository.getById(id);
            assertThat(upload).isNotNull();

            var status=upload.status();
            var expectedStatus=transformation.equals(transformations.getLast()) ? UploadStatus.READY : UploadStatus.PROCESSING;
            assertThat(status).isEqualTo(expectedStatus);
        }
    }
}
