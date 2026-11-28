package com.example.media.json;

import com.example.media.contract.transformation.*;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The JSON shape of the specs is persisted in the registry and sent to workers.
 * These tests are the shape lock: the discriminators and property names must not drift.
 */
class TransformationSpecJsonTest {

    ObjectMapper objectMapper = MediaJson.create();

    @Test
    void imageSpecRoundTripKeepsThePersistedShape() {
        TransformationSpec spec = new ImageTransformationSpec(
                new LimitResolution(100, LimitResolution.Mode.KEEP_ASPECT_RATIO),
                null,
                new AspectRatio(1, 1, AspectRatio.Mode.FILL),
                new ImageEncoding.Webp(50)
        );

        var json = objectMapper.writeValueAsString(spec);

        assertThat(json)
                .contains("\"type\":\"image\"")
                .contains("\"type\":\"webp\"")
                .contains("\"quality\":50")
                .contains("\"limitWidth\"")
                .contains("\"aspectRatio\"");
        assertThat(objectMapper.readValue(json, TransformationSpec.class)).isEqualTo(spec);
    }

    @Test
    void mp4SpecRoundTripKeepsThePersistedShape() {
        TransformationSpec spec = new VideoTransformationSpec(VideoEncoding.Mp4.builder()
                .video(VideoEncoding.VideoSettings.builder()
                        .codec(VideoEncoding.VideoCodec.H264)
                        .constantRateFactor(23)
                        .videoBitrate(2_000_000)
                        .frameRate(30)
                        .limitWidth(new LimitResolution(1280, LimitResolution.Mode.KEEP_ASPECT_RATIO))
                        .groupOfFrames(60)
                        .build())
                .audio(VideoEncoding.AudioSettings.builder()
                        .codec(VideoEncoding.AudioCodec.AAC)
                        .bitrate(128_000)
                        .channels(2)
                        .sampleRateHz(48_000)
                        .build())
                .fastStart(true)
                .build());

        var json = objectMapper.writeValueAsString(spec);

        assertThat(json)
                .contains("\"type\":\"video\"")
                .contains("\"type\":\"mp4\"")
                .contains("\"video\"")
                .contains("\"audio\"")
                .contains("\"codec\":\"H264\"")
                .contains("\"constantRateFactor\":23")
                .contains("\"fastStart\":true")
                .doesNotContain("null");
        assertThat(objectMapper.readValue(json, TransformationSpec.class)).isEqualTo(spec);
    }

    @Test
    void hlsSpecRoundTripKeepsThePersistedShape() {
        TransformationSpec spec = new VideoTransformationSpec(VideoEncoding.Hls.builder()
                .video(VideoEncoding.VideoSettings.builder()
                        .codec(VideoEncoding.VideoCodec.H264)
                        .preset(VideoEncoding.VideoEncodingPreset.veryfast)
                        .frameRate(30)
                        .build())
                .audio(VideoEncoding.AudioSettings.builder()
                        .codec(VideoEncoding.AudioCodec.AAC)
                        .bitrate(96_000)
                        .build())
                .segmentSeconds(6)
                .tracks(List.of(
                        VideoEncoding.Hls.HlsTrack.builder()
                                .name("high")
                                .limitHeight(new LimitResolution(720, LimitResolution.Mode.KEEP_ASPECT_RATIO))
                                .videoBitrate(2_800_000)
                                .constantRateFactor(20)
                                .audioBitrate(128_000)
                                .build(),
                        VideoEncoding.Hls.HlsTrack.builder()
                                .name("low")
                                .limitHeight(new LimitResolution(360, LimitResolution.Mode.KEEP_ASPECT_RATIO))
                                .videoBitrate(1_200_000)
                                .constantRateFactor(23)
                                .audioBitrate(96_000)
                                .build()))
                .build());

        var json = objectMapper.writeValueAsString(spec);

        assertThat(json)
                .contains("\"type\":\"video\"")
                .contains("\"type\":\"hls\"")
                .contains("\"segmentSeconds\":6")
                .contains("\"tracks\"")
                .contains("\"name\":\"high\"")
                .contains("\"name\":\"low\"")
                .contains("\"codec\":\"H264\"")
                .doesNotContain("null");
        assertThat(objectMapper.readValue(json, TransformationSpec.class)).isEqualTo(spec);
    }

    @Test
    void imageEncodingRoundTrip() {
        ImageEncoding encoding = new ImageEncoding.Jpeg(80);

        var json = objectMapper.writeValueAsString(encoding);

        assertThat(json).contains("\"type\":\"jpeg\"").contains("\"quality\":80");
        assertThat(objectMapper.readValue(json, ImageEncoding.class)).isEqualTo(encoding);
    }

    @Test
    void aMinimalSpecRoundTripsToo() {
        TransformationSpec spec = new ImageTransformationSpec(null, null, null, new ImageEncoding.Webp(100));

        var json = objectMapper.writeValueAsString(spec);

        assertThat(objectMapper.readValue(json, TransformationSpec.class)).isEqualTo(spec);
    }
}