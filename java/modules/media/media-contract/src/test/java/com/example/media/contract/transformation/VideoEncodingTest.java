package com.example.media.contract.transformation;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The persisted video encoding vocabulary validates itself before it reaches the worker. These
 * checks are the first line of defence for the JSON that the registry stores and the queue carries.
 */
class VideoEncodingTest {

    @Test
    void mp4RequiresVideoAndAudioSettings() {
        assertThatThrownBy(() -> VideoEncoding.Mp4.builder().build())
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> VideoEncoding.Mp4.builder()
                .video(VideoEncoding.VideoSettings.h264())
                .build())
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void videoSettingsRejectsOutOfRangeValues() {
        assertThatThrownBy(() -> VideoEncoding.VideoSettings.builder()
                .codec(VideoEncoding.VideoCodec.H264)
                .constantRateFactor(52)
                .build())
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> VideoEncoding.VideoSettings.builder()
                .codec(VideoEncoding.VideoCodec.H264)
                .videoBitrate(0)
                .build())
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> VideoEncoding.VideoSettings.builder()
                .codec(VideoEncoding.VideoCodec.H264)
                .frameRate(0)
                .build())
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> VideoEncoding.VideoSettings.builder()
                .codec(VideoEncoding.VideoCodec.H264)
                .groupOfFrames(0)
                .build())
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void audioSettingsRejectsInvalidValues() {
        assertThatThrownBy(() -> VideoEncoding.AudioSettings.builder()
                .codec(VideoEncoding.AudioCodec.AAC)
                .bitrate(0)
                .build())
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> VideoEncoding.AudioSettings.builder()
                .codec(VideoEncoding.AudioCodec.AAC)
                .channels(0)
                .build())
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> VideoEncoding.AudioSettings.builder()
                .codec(VideoEncoding.AudioCodec.AAC)
                .sampleRateHz(0)
                .build())
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void hlsNeedsAtLeastOneTrack() {
        assertThatThrownBy(() -> hls(List.of()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void hlsRejectsTooManyTracks() {
        var tracks = IntStream.range(0, 11)
                .mapToObj(i -> track("track-" + i))
                .toList();

        assertThatThrownBy(() -> hls(tracks))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void hlsRejectsDuplicateAndUnsafeTrackNames() {
        assertThatThrownBy(() -> hls(List.of(track("high"), track("high"))))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> VideoEncoding.Hls.HlsTrack.builder()
                .name("high quality")
                .videoBitrate(1_000_000)
                .build())
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> VideoEncoding.Hls.HlsTrack.builder()
                .name("../escape")
                .videoBitrate(1_000_000)
                .build())
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> VideoEncoding.Hls.HlsTrack.builder()
                .name("%v")
                .videoBitrate(1_000_000)
                .build())
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void hlsNeedsAResolvableVideoBitratePerTrack() {
        assertThatThrownBy(() -> VideoEncoding.Hls.builder()
                .video(VideoEncoding.VideoSettings.h264())
                .audio(VideoEncoding.AudioSettings.aac())
                .segmentSeconds(6)
                .tracks(List.of(VideoEncoding.Hls.HlsTrack.builder().name("high").build()))
                .build())
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void hlsRejectsTrackAudioBitrateWhenAudioIsDisabled() {
        assertThatThrownBy(() -> VideoEncoding.Hls.builder()
                .video(VideoEncoding.VideoSettings.h264())
                .audio(VideoEncoding.AudioSettings.noAudio())
                .segmentSeconds(6)
                .tracks(List.of(VideoEncoding.Hls.HlsTrack.builder()
                        .name("high")
                        .videoBitrate(1_000_000)
                        .audioBitrate(128_000)
                        .build()))
                .build())
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void hlsRejectsAnInvalidSegmentLength() {
        assertThatThrownBy(() -> VideoEncoding.Hls.builder()
                .video(VideoEncoding.VideoSettings.h264())
                .audio(VideoEncoding.AudioSettings.aac())
                .segmentSeconds(0)
                .tracks(List.of(track("high")))
                .build())
                .isInstanceOf(IllegalArgumentException.class);
    }

    /**
     * Builds an HLS encoding; the constructor validation is the behavior under test.
     */
    private static void hls(List<VideoEncoding.Hls.HlsTrack> tracks) {
        VideoEncoding.Hls.builder()
                .video(VideoEncoding.VideoSettings.h264())
                .audio(VideoEncoding.AudioSettings.aac())
                .segmentSeconds(6)
                .tracks(tracks)
                .build();
    }

    private static VideoEncoding.Hls.HlsTrack track(String name) {
        return VideoEncoding.Hls.HlsTrack.builder()
                .name(name)
                .videoBitrate(1_000_000)
                .build();
    }
}
