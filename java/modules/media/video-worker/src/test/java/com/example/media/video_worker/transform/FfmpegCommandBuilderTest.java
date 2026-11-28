package com.example.media.video_worker.transform;

import com.example.media.contract.transformation.LimitResolution;
import com.example.media.contract.transformation.VideoEncoding;
import com.example.media.contract.transformation.VideoTransformationSpec;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Locks the ffmpeg argument shape without running ffmpeg. The integration suite is the behavior
 * lock; these tests make the mapping cheap to inspect and change.
 */
class FfmpegCommandBuilderTest {

    private final FfmpegCommandBuilder builder = new FfmpegCommandBuilder();

    @Test
    void mp4MapsTheSettingsToFfmpegArguments() {
        var spec = new VideoTransformationSpec(VideoEncoding.Mp4.builder()
                .video(VideoEncoding.VideoSettings.builder()
                        .codec(VideoEncoding.VideoCodec.H264)
                        .preset(VideoEncoding.VideoEncodingPreset.veryfast)
                        .constantRateFactor(23)
                        .videoBitrate(2_000_000)
                        .frameRate(30)
                        .limitWidth(new LimitResolution(1280, LimitResolution.Mode.KEEP_ASPECT_RATIO))
                        .limitHeight(new LimitResolution(720, LimitResolution.Mode.KEEP_ASPECT_RATIO))
                        .build())
                .audio(VideoEncoding.AudioSettings.builder()
                        .codec(VideoEncoding.AudioCodec.AAC)
                        .bitrate(128_000)
                        .channels(2)
                        .sampleRateHz(48_000)
                        .build())
                .build());

        var command = builder.build(Path.of("source.mp4"), Path.of("out"), spec, true, 30);
        var args = command.builder().build();

        assertThat(command.entryPoint()).isEqualTo(Path.of("out", "result.mp4"));
        assertThat(args).containsSubsequence("-i", "source.mp4");
        assertThat(args).containsSubsequence("-vcodec", "libx264");
        assertThat(args).containsSubsequence("-pix_fmt", "yuv420p");
        assertThat(args).containsSubsequence("-preset", "veryfast");
        assertThat(args).containsSubsequence("-crf", "23");
        assertThat(args).containsSubsequence("-b:v", "2000000");
        assertThat(args).containsSubsequence("-r", "30/1");
        assertThat(args).containsSubsequence("-vf",
                "scale=w=min(iw\\,1280):h=min(ih\\,720):force_original_aspect_ratio=decrease:force_divisible_by=2");
        assertThat(args).containsSubsequence("-acodec", "aac");
        assertThat(args).containsSubsequence("-b:a", "128000");
        assertThat(args).containsSubsequence("-ac", "2");
        assertThat(args).containsSubsequence("-ar", "48000");
        assertThat(args).containsSubsequence("-movflags", "+faststart");
        assertThat(args).containsSubsequence("-map", "0:v:0", "-map", "0:a:0?");
    }

    @Test
    void mp4WithoutLimitsHasNoVideoFilterAndCanDisableAudioAndFastStart() {
        var spec = new VideoTransformationSpec(VideoEncoding.Mp4.builder()
                .video(VideoEncoding.VideoSettings.h264())
                .audio(VideoEncoding.AudioSettings.noAudio())
                .fastStart(false)
                .build());

        var args = builder.build(Path.of("source.mp4"), Path.of("out"), spec, true, 30)
                .builder().build();

        assertThat(args).doesNotContain("-vf");
        assertThat(args).doesNotContain("-movflags");
        assertThat(args).contains("-an");
        assertThat(args).doesNotContain("-acodec");
    }

    @Test
    void mp4RoundsAnOddWidthLimitDownToAnEvenDimension() {
        var spec = new VideoTransformationSpec(VideoEncoding.Mp4.builder()
                .video(VideoEncoding.VideoSettings.builder()
                        .codec(VideoEncoding.VideoCodec.H264)
                        .limitWidth(new LimitResolution(201, LimitResolution.Mode.KEEP_ASPECT_RATIO))
                        .build())
                .audio(VideoEncoding.AudioSettings.aac())
                .build());

        var args = builder.build(Path.of("source.mp4"), Path.of("out"), spec, true, 30)
                .builder().build();

        assertThat(args).containsSubsequence("-vf", "scale=w=min(iw\\,200):h=-2");
    }

    @Test
    void hlsMapsTheLadderAndThePerTrackOverrides() {
        var spec = hlsSpec();

        var command = builder.build(Path.of("source.mp4"), Path.of("out"), spec, true, 30);
        var args = command.builder().build();

        assertThat(command.entryPoint()).isEqualTo(Path.of("out", "master.m3u8"));
        assertThat(args).containsSubsequence("-vcodec", "libx264");
        assertThat(args).containsSubsequence("-pix_fmt", "yuv420p");
        assertThat(args).containsSubsequence("-preset", "veryfast");
        assertThat(args).containsSubsequence("-r", "30/1");
        assertThat(args).containsSubsequence("-g", "180");
        assertThat(args).containsSubsequence("-sc_threshold", "0");
        assertThat(args).containsSubsequence("-hls_time", "6");
        assertThat(args).containsSubsequence("-hls_segment_type", "fmp4");
        assertThat(args).containsSubsequence("-hls_flags", "independent_segments");
        assertThat(args).containsSubsequence("-hls_playlist_type", "vod");
        assertThat(args).containsSubsequence("-hls_list_size", "0");
        assertThat(args).containsSubsequence("-master_pl_name", "master.m3u8");
        assertThat(args).containsSubsequence("-hls_segment_filename", "out/var-%v/segment-%d.m4s");
        assertThat(args).containsSubsequence("-var_stream_map", "v:0,a:0,name:high v:1,a:1,name:low");
        assertThat(args).containsSubsequence("-filter_complex",
                "[0:v]split=2[v0][v1];[v0]scale=w=-2:h=min(ih\\,720)[v0out];[v1]scale=w=-2:h=min(ih\\,360)[v1out]");
        assertThat(args).containsSubsequence("-map", "[v0out]", "-map", "0:a:0", "-map", "[v1out]", "-map", "0:a:0");
        assertThat(args).containsSubsequence("-b:v:0", "2800000");
        assertThat(args).containsSubsequence("-crf:v:0", "20");
        assertThat(args).containsSubsequence("-b:a:0", "128000");
        assertThat(args).containsSubsequence("-b:v:1", "1200000");
        assertThat(args).containsSubsequence("-crf:v:1", "23");
        assertThat(args).containsSubsequence("-b:a:1", "96000");
        assertThat(args).containsSubsequence("-acodec", "aac");
        assertThat(args).doesNotContain("-b:a");
    }

    @Test
    void hlsWithoutAudioHasNoAudioStreams() {
        var spec = new VideoTransformationSpec(VideoEncoding.Hls.builder()
                .video(VideoEncoding.VideoSettings.h264())
                .audio(VideoEncoding.AudioSettings.aac())
                .segmentSeconds(6)
                .tracks(List.of(VideoEncoding.Hls.HlsTrack.builder()
                        .name("single")
                        .videoBitrate(1_000_000)
                        .build()))
                .build());

        var args = builder.build(Path.of("source.mp4"), Path.of("out"), spec, false, 30)
                .builder().build();

        assertThat(args).containsSubsequence("-var_stream_map", "v:0,name:single");
        assertThat(args).doesNotContain("-acodec");
        assertThat(args).contains("-an");
    }

    @Test
    void hlsInheritsThePlaylistLimitAndUsesOneNullFilterForTheUnscaledTrack() {
        var spec = new VideoTransformationSpec(VideoEncoding.Hls.builder()
                .video(VideoEncoding.VideoSettings.builder()
                        .codec(VideoEncoding.VideoCodec.H264)
                        .limitHeight(new LimitResolution(480, LimitResolution.Mode.KEEP_ASPECT_RATIO))
                        .build())
                .audio(VideoEncoding.AudioSettings.noAudio())
                .segmentSeconds(6)
                .tracks(List.of(
                        VideoEncoding.Hls.HlsTrack.builder().name("scaled").videoBitrate(1_000_000).build(),
                        VideoEncoding.Hls.HlsTrack.builder().name("unscaled").videoBitrate(1_000_000).build()))
                .build());

        var args = builder.build(Path.of("source.mp4"), Path.of("out"), spec, false, 30)
                .builder().build();

        assertThat(args).containsSubsequence("-filter_complex",
                "[0:v]split=2[v0][v1];[v0]scale=w=-2:h=min(ih\\,480)[v0out];[v1]scale=w=-2:h=min(ih\\,480)[v1out]");
        assertThat(args).contains("-an");
    }

    private static VideoTransformationSpec hlsSpec() {
        return new VideoTransformationSpec(VideoEncoding.Hls.builder()
                .video(VideoEncoding.VideoSettings.builder()
                        .codec(VideoEncoding.VideoCodec.H264)
                        .preset(VideoEncoding.VideoEncodingPreset.veryfast)
                        .frameRate(30)
                        .constantRateFactor(23)
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
    }
}
