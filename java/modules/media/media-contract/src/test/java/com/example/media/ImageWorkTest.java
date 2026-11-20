package com.example.media;

import com.example.media.asset.AssetLocation;
import com.example.media.identity.*;
import com.example.media.registry.Publication;
import com.example.media.registry.VariantRegistration;
import com.example.media.transformation.AspectRatio;
import com.example.media.transformation.ImageEncoding;
import com.example.media.transformation.ImageTransformation;
import com.example.media.upload.AttemptStatus;
import com.example.media.upload.UploadAttempt;
import com.example.media.worker.ImageWork;
import com.example.media.worker.ReservedImageResult;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ImageWorkTest {
    private static final UUID SOURCE_ID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

    @Test
    void imageEncodingKeepsThePrototypeContentTypesAndQualityRange() {
        assertThat(new ImageEncoding.Webp(50).contentType()).isEqualTo("image/webp");
        assertThat(new ImageEncoding.Jpeg(100).contentType()).isEqualTo("image/jpeg");
        assertThat(new ImageEncoding.Jpeg(1).extension()).isEqualTo("jpg");
        assertThatThrownBy(() -> new ImageEncoding.Webp(0)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new ImageEncoding.Webp(101)).isInstanceOf(IllegalArgumentException.class);

        var profile = new ImageTransformation(null, null, new AspectRatio(1, 1, AspectRatio.Mode.FILL), new ImageEncoding.Webp(100));
        assertThat(profile.aspectRatio()).isNotNull();
        assertThat(new ImageTransformation(null, null, null, new ImageEncoding.Webp(50)).encoding().directory()).isFalse();
    }

    @Test
    void attemptFinishTimeFollowsTheStatus() {
        var finishedAt = Instant.parse("2026-01-01T00:00:00Z");
        assertThatThrownBy(() -> attempt(AttemptStatus.SUCCESS, null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> attempt(AttemptStatus.PENDING, finishedAt)).isInstanceOf(IllegalArgumentException.class);
        assertThat(attempt(AttemptStatus.FAILED, finishedAt).status()).isEqualTo(AttemptStatus.FAILED);
        assertThat(new UploadKey("a".repeat(100)).value()).hasSize(100);
        assertThatThrownBy(() -> new UploadKey("a".repeat(101))).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void workAndPublicationRequireDistinctAssetIds() {
        var source = location(SOURCE_ID, "original.jpg");
        var eager = reserved(UUID.randomUUID(), "profile");
        var work = new ImageWork(new AttemptId(UUID.randomUUID()), new UploadKey("post"), source, reserved(UUID.randomUUID(), "optimize"), List.of(eager));

        assertThat(work.eager()).containsExactly(eager);
        assertThatThrownBy(() -> new ImageWork(work.attemptId(), work.uploadKey(), source, reserved(SOURCE_ID, "optimize"), List.of()))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new ImageWork(work.attemptId(), work.uploadKey(), source, null, List.of(eager, reserved(UUID.randomUUID(), "profile"))))
                .isInstanceOf(IllegalArgumentException.class);

        assertThat(new Publication(work.attemptId(), work.uploadKey(), source.assetId(), List.of(registration(eager.assetId()))).variants()).hasSize(1);
        assertThatThrownBy(() -> new Publication(work.attemptId(), work.uploadKey(), source.assetId(), List.of(registration(source.assetId()))))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private static UploadAttempt attempt(AttemptStatus status, Instant finishedAt) {
        return new UploadAttempt(
                new AttemptId(UUID.randomUUID()),
                new UploadKey("post"),
                List.of(),
                null,
                new AssetId(SOURCE_ID),
                Instant.parse("2026-01-01T00:00:00Z"),
                status,
                finishedAt
        );
    }

    private static AssetLocation location(UUID assetId, String relativePath) {
        return new AssetLocation("home", "uploads", new AssetId(assetId), List.of(relativePath));
    }

    private static ReservedImageResult reserved(UUID assetId, String name) {
        return new ReservedImageResult(
                new AssetId(assetId),
                location(assetId, "default.webp"),
                new TransformationVersionId(UUID.randomUUID()),
                name,
                new ImageTransformation(null, null, null, new ImageEncoding.Webp(100))
        );
    }

    private static VariantRegistration registration(AssetId assetId) {
        return new VariantRegistration(new NamedTransformationId(UUID.randomUUID()), new TransformationVersionId(UUID.randomUUID()), assetId);
    }
}
