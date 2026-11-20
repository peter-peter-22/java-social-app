package com.example.media.registry.transformation;

import com.example.cockroach_db.CockroachIntegrationTest;
import com.example.media.contract.port.TransformationCatalog;
import com.example.media.contract.transformation.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class JpaTransformationCatalogIT extends CockroachIntegrationTest {
    @Autowired
    private TransformationCatalog transformationCatalog;
    @Autowired
    private TransformationVersionCrudRepository transformationVersionCrudRepository;

    @Test
    void registersAndResolvesATransformation() {
        var name = name();
        var spec = spec(80);

        var registered = transformationCatalog.register(new NewNamedTransformation(name, spec));

        assertThat(registered.name()).isEqualTo(name);
        assertThat(registered.spec()).isEqualTo(spec);
        assertThat(registered.currentVersionId()).isNotNull();
        assertThat(transformationCatalog.resolve(name)).isEqualTo(registered);
    }

    @Test
    void reusesTheExistingVersionForTheSameSpec() {
        var name = name();
        var insert = new NewNamedTransformation(name, spec(80));

        transformationCatalog.register(insert);
        transformationCatalog.register(insert);
        transformationCatalog.register(insert);

        assertThat(transformationVersionCrudRepository.countByNamedTransformationEntity_Name(name)).isOne();
    }

    @Test
    void keepsThePreviousVersionWhenTheSpecChanges() {
        var name = name();

        var first = transformationCatalog.register(new NewNamedTransformation(name, spec(80)));
        var second = transformationCatalog.register(new NewNamedTransformation(name, spec(60)));

        assertThat(transformationCatalog.resolve(name).spec()).isEqualTo(spec(60));
        assertThat(second.id()).isEqualTo(first.id());
        assertThat(second.currentVersionId()).isNotEqualTo(first.currentVersionId());
        assertThat(transformationVersionCrudRepository.countByNamedTransformationEntity_Name(name)).isEqualTo(2);
    }

    @Test
    void resolveAllResolvesEveryNameOrNoneOfThem() {
        var first = name();
        var second = name();
        transformationCatalog.register(new NewNamedTransformation(first, spec(80)));
        transformationCatalog.register(new NewNamedTransformation(second, spec(60)));

        var resolved = transformationCatalog.resolveAll(List.of(first, second));
        assertThat(resolved).extracting(NamedTransformation::name).containsExactly(first, second);

        assertThatThrownBy(() -> transformationCatalog.resolveAll(List.of(first, name())))
                .isInstanceOf(TransformationNotFoundException.class);
    }

    @Test
    void missingTransformationThrowsException() {
        assertThatThrownBy(() -> transformationCatalog.resolve(name()))
                .isInstanceOf(TransformationNotFoundException.class);
    }

    private String name() {
        return "transformation-" + UUID.randomUUID().toString().substring(0, 20);
    }

    private TransformationSpec spec(int quality) {
        return new ImageTransformationSpec(null, null, null, new ImageEncoding.Jpeg(quality));
    }
}
