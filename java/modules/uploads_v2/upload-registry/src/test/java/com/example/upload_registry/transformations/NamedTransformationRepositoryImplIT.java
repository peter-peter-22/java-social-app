package com.example.upload_registry.transformations;

import com.example.cockroach_db.CockroachIntegrationTest;
import com.example.uploads_api.transformations.named_transformation_repository.InsertNamedImageTransformation;
import com.example.uploads_api.transformations.operations.ImageEncodings;
import com.example.uploads_api.transformations.operations.ImageTransformationOperations;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class NamedTransformationRepositoryImplIT extends CockroachIntegrationTest {
    @Autowired
    private NamedTransformationRepositoryImpl namedTransformationRepository;
    @Autowired
    private TransformationVersionRepository transformationVersionRepository;

    @Test
    void setsAndGetsTransformation() {
        var name = name();
        var operations = operations(80);

        namedTransformationRepository.set(new InsertNamedImageTransformation(name, operations));

        assertThat(namedTransformationRepository.get(name).getOperations()).isEqualTo(operations);
    }

    @Test
    void reusesExistingVersionForSameOperations() {
        var name = name();
        var transformation = new InsertNamedImageTransformation(name, operations(80));

        namedTransformationRepository.set(transformation);
        namedTransformationRepository.set(transformation);
        namedTransformationRepository.set(transformation);

        assertThat(transformationVersionRepository.countByNamedTransformationEntity_Name(name)).isOne();
    }

    @Test
    void keepsPreviousVersionWhenOperationsChange() {
        var name = name();
        var firstOperations = operations(80);
        var newOperations = operations(60);

        namedTransformationRepository.set(new InsertNamedImageTransformation(name, firstOperations));
        namedTransformationRepository.set(new InsertNamedImageTransformation(name, newOperations));

        assertThat(namedTransformationRepository.get(name).getOperations()).isEqualTo(newOperations);
        assertThat(transformationVersionRepository.countByNamedTransformationEntity_Name(name)).isEqualTo(2);
    }

    private String name() {
        return "transformation-" + UUID.randomUUID().toString().substring(0, 20);
    }

    private ImageTransformationOperations operations(int quality) {
        return ImageTransformationOperations.builder()
                .encoding(ImageEncodings.Jpeg.builder().quality(quality).build())
                .build();
    }
}
