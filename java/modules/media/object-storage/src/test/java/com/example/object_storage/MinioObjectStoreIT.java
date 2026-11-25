package com.example.object_storage;

import com.example.media.contract.asset.AssetPlacement;
import com.example.media.contract.object.ObjectLifetime;
import com.example.media.contract.object.ObjectPrefix;
import com.example.media.contract.object.ObjectRef;
import com.example.media.contract.port.ObjectStore;
import com.example.object_storage.regional.MinioConfig;
import io.minio.GetObjectTagsArgs;
import io.minio.SetObjectTagsArgs;
import io.minio.errors.ErrorResponseException;
import org.jspecify.annotations.NullMarked;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The port contract of {@link ObjectStore}, exercised against the Minio adapter.
 * The expiration tags are asserted through the Minio client because the tag names stay
 * inside this adapter; the application services only see {@link ObjectLifetime}.
 */
@SpringBootTest
@NullMarked
public class MinioObjectStoreIT extends MinioIntegrationTest {
    private static final String CONTENT_TYPE = "text/plain";

    @Autowired
    private ObjectStore objectStore;
    @Autowired
    private MinioConfig minioConfig;

    @TempDir
    Path tempDir;

    private final ObjectPrefix prefix =
            new ObjectPrefix("test", "uploads", UUID.randomUUID().toString());

    @Test
    void homePlacementReadsTheConfiguredRegionAndBucket() {
        assertThat(objectStore.homePlacement()).isEqualTo(new AssetPlacement("test", "uploads"));
    }

    @Test
    void writesReadsAndDeletesAnObjectFromAStream() throws IOException {
        var ref = prefix.resolve("original.jpg");
        var content = Files.readAllBytes(new ClassPathResource("test.txt").getFile().toPath());

        assertThat(objectStore.exists(ref)).isFalse();
        try (var data = new ByteArrayInputStream(content)) {
            objectStore.put(ref, data, content.length, CONTENT_TYPE, ObjectLifetime.TEMPORARY);
        }
        assertThat(objectStore.exists(ref)).isTrue();

        try (var read = objectStore.get(ref)) {
            assertThat(read.readAllBytes()).isEqualTo(content);
        }

        objectStore.delete(ref);
        assertThat(objectStore.exists(ref)).isFalse();
    }

    @Test
    void writesAndDownloadsAnObjectFromAFile() throws IOException {
        var ref = prefix.resolve("original.jpg");
        var source = new ClassPathResource("test.txt").getFile().toPath();
        var content = Files.readAllBytes(source);
        var target = tempDir.resolve("downloaded.txt");

        objectStore.put(ref, source, CONTENT_TYPE, ObjectLifetime.TEMPORARY);
        assertThat(objectStore.exists(ref)).isTrue();
        objectStore.download(ref, target);

        assertThat(Files.readAllBytes(target)).isEqualTo(content);
    }

    @Test
    void refusesToOverwriteAnExistingObject() throws IOException {
        var ref = prefix.resolve("original.jpg");
        var source = new ClassPathResource("test.txt").getFile().toPath();
        objectStore.put(ref, source, CONTENT_TYPE, ObjectLifetime.TEMPORARY);

        try (var data = new ByteArrayInputStream(new byte[]{1})) {
            assertThatThrownBy(() -> objectStore.put(ref, data, 1, CONTENT_TYPE, ObjectLifetime.TEMPORARY))
                    .isInstanceOf(RuntimeException.class);
        }
    }

    @Test
    void setLifetimeMovesTheExpirationTagOfEveryGivenObject() throws Exception {
        var original = prefix.resolve("original.jpg");
        var variant = prefix.resolve("profile.webp");
        var source = new ClassPathResource("test.txt").getFile().toPath();
        objectStore.put(original, source, CONTENT_TYPE, ObjectLifetime.TEMPORARY);
        objectStore.put(variant, source, CONTENT_TYPE, ObjectLifetime.TEMPORARY);

        assertThat(tagsOf(original)).isEqualTo(Map.of("expires", "enabled"));
        assertThat(tagsOf(variant)).isEqualTo(Map.of("expires", "enabled"));

        objectStore.setLifetime(List.of(original, variant), ObjectLifetime.PERMANENT);

        assertThat(tagsOf(original)).isEqualTo(Map.of("expires", "disabled"));
        assertThat(tagsOf(variant)).isEqualTo(Map.of("expires", "disabled"));
    }

    @Test
    void setLifetimeReplacesTheWholeTagSetOfAnObject() throws Exception {
        var ref = prefix.resolve("original.jpg");
        var source = new ClassPathResource("test.txt").getFile().toPath();
        objectStore.put(ref, source, CONTENT_TYPE, ObjectLifetime.TEMPORARY);
        // A tag beyond the lifetime tag, written through the Minio client directly.
        minioConfig.getClient(ref.region()).setObjectTags(SetObjectTagsArgs.builder()
                .bucket(ref.bucket())
                .object(ref.key())
                .tags(Map.of("expires", "enabled", "extra", "tag"))
                .build());
        assertThat(tagsOf(ref)).isEqualTo(Map.of("expires", "enabled", "extra", "tag"));

        objectStore.setLifetime(List.of(ref), ObjectLifetime.PERMANENT);

        // Pinned semantics: the Minio tag write replaces the tag set instead of merging into it,
        // so the extra tag does not survive and the kept object carries exactly the lifetime tag.
        assertThat(tagsOf(ref)).isEqualTo(Map.of("expires", "disabled"));
    }

    @Test
    void setLifetimeFailsWhenAnObjectIsMissing() throws IOException {
        var present = prefix.resolve("original.jpg");
        var missing = prefix.resolve("missing.jpg");
        var source = new ClassPathResource("test.txt").getFile().toPath();
        objectStore.put(present, source, CONTENT_TYPE, ObjectLifetime.TEMPORARY);

        // The finalize relies on this failure: making the kept objects permanent proves that they
        // exist, so the application service does not stat every object before the commit.
        assertThatThrownBy(() -> objectStore.setLifetime(List.of(present, missing), ObjectLifetime.PERMANENT))
                .isInstanceOfSatisfying(RuntimeException.class, failure -> assertThat(failure.getCause())
                        .isInstanceOfSatisfying(ErrorResponseException.class, cause -> assertThat(
                                cause.errorResponse().code()).isEqualTo("NoSuchKey")));
    }

    private Map<String, String> tagsOf(ObjectRef ref) throws Exception {
        return minioConfig.getClient(ref.region()).getObjectTags(
                GetObjectTagsArgs.builder().bucket(ref.bucket()).object(ref.key()).build()).get();
    }
}
